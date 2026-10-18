package com.blendroid.app.ui.editor

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.blendroid.app.engine.model.EditorMode
import com.blendroid.app.engine.model.MeshPrimitiveType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.ShadingMode
import com.blendroid.app.engine.model.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class BlendroidGLRenderer : GLSurfaceView.Renderer {

    var scene: SceneGraph? = null

    // Orbit Camera State
    var yaw = 35f
    var pitch = 25f
    var zoom = 15f
    var panX = 0f
    var panY = 0f

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var shaderProgram = 0
    private var lineProgram = 0

    // Handles for Main Shader
    private var positionHandle = 0
    private var normalHandle = 0
    private var mvpMatrixHandle = 0
    private var modelMatrixHandle = 0
    private var colorHandle = 0
    private var lightPosHandle = 0
    private var shadingModeHandle = 0

    // Handles for Line Shader
    private var linePosHandle = 0
    private var lineMvpHandle = 0
    private var lineColorHandle = 0

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        attribute vec4 aPosition;
        attribute vec3 aNormal;
        varying vec3 vNormal;
        varying vec3 vFragPos;

        void main() {
            vFragPos = vec3(uModelMatrix * aPosition);
            vNormal = mat3(uModelMatrix) * aNormal;
            gl_Position = uMVPMatrix * aPosition;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        uniform vec3 uLightPos;
        uniform int uShadingMode; // 0: Wireframe, 1: Solid, 2: Material, 3: Rendered
        varying vec3 vNormal;
        varying vec3 vFragPos;

        void main() {
            if (uShadingMode == 0) {
                gl_FragColor = uColor;
                return;
            }

            vec3 norm = normalize(vNormal);
            vec3 lightDir = normalize(uLightPos - vFragPos);

            // Ambient
            float ambientStrength = (uShadingMode == 3) ? 0.3 : 0.4;
            vec3 ambient = ambientStrength * uColor.rgb;

            // Diffuse
            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * uColor.rgb;

            // Specular
            float specularStrength = (uShadingMode == 3) ? 0.5 : 0.2;
            vec3 viewDir = normalize(-vFragPos);
            vec3 reflectDir = reflect(-lightDir, norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16.0);
            vec3 specular = specularStrength * vec3(1.0, 1.0, 1.0) * spec;

            vec3 finalColor = ambient + diffuse + specular;
            gl_FragColor = vec4(finalColor, uColor.a);
        }
    """.trimIndent()

    private val lineVertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
        }
    """.trimIndent()

    private val lineFragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.12f, 0.14f, 0.17f, 1.0f) // Dark Blender background
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Compile Mesh Shader
        val vertShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        shaderProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertShader)
            GLES20.glAttachShader(it, fragShader)
            GLES20.glLinkProgram(it)
        }

        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        normalHandle = GLES20.glGetAttribLocation(shaderProgram, "aNormal")
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        modelMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uModelMatrix")
        colorHandle = GLES20.glGetUniformLocation(shaderProgram, "uColor")
        lightPosHandle = GLES20.glGetUniformLocation(shaderProgram, "uLightPos")
        shadingModeHandle = GLES20.glGetUniformLocation(shaderProgram, "uShadingMode")

        // Compile Line Shader
        val lineVertShader = loadShader(GLES20.GL_VERTEX_SHADER, lineVertexShaderCode)
        val lineFragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, lineFragmentShaderCode)
        lineProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, lineVertShader)
            GLES20.glAttachShader(it, lineFragShader)
            GLES20.glLinkProgram(it)
        }

        linePosHandle = GLES20.glGetAttribLocation(lineProgram, "aPosition")
        lineMvpHandle = GLES20.glGetUniformLocation(lineProgram, "uMVPMatrix")
        lineColorHandle = GLES20.glGetUniformLocation(lineProgram, "uColor")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 500f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Orbit Camera View Matrix setup
        val radYaw = Math.toRadians(yaw.toDouble()).toFloat()
        val radPitch = Math.toRadians(pitch.toDouble()).toFloat()

        val camX = zoom * cos(radPitch) * sin(radYaw) + panX
        val camY = zoom * sin(radPitch) + panY
        val camZ = zoom * cos(radPitch) * cos(radYaw)

        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, panX, panY, 0f, 0f, 1f, 0f)

        // Draw 3D Grid Floor & Origin Axes Gizmo
        drawGridFloorAndAxes()

        // Draw Scene Objects
        val currentScene = scene ?: return
        for (obj in currentScene.objects) {
            if (!obj.isVisible) continue

            val isSelected = obj.id == currentScene.activeObjectId

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, obj.transform.position.x, obj.transform.position.y, obj.transform.position.z)
            Matrix.rotateM(modelMatrix, 0, obj.transform.rotation.x, 1f, 0f, 0f)
            Matrix.rotateM(modelMatrix, 0, obj.transform.rotation.y, 0f, 1f, 0f)
            Matrix.rotateM(modelMatrix, 0, obj.transform.rotation.z, 0f, 0f, 1f)
            Matrix.scaleM(modelMatrix, 0, obj.transform.scale.x, obj.transform.scale.y, obj.transform.scale.z)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

            when (obj.type) {
                MeshPrimitiveType.LIGHT, MeshPrimitiveType.CAMERA -> {
                    val color = if (obj.type == MeshPrimitiveType.LIGHT) floatArrayOf(0.96f, 0.62f, 0.04f, 1f) else floatArrayOf(0.57f, 0.2f, 0.91f, 1f)
                    drawSimplePointGizmo(modelMatrix, color)
                }
                else -> {
                    val verts = obj.mesh.vertices
                    val faces = obj.mesh.faces

                    if (verts.isNotEmpty() && faces.isNotEmpty()) {
                        drawMesh(obj, currentScene.shadingMode, isSelected)
                    }
                }
            }
        }
    }

    private fun drawGridFloorAndAxes() {
        GLES20.glUseProgram(lineProgram)

        val gridLines = mutableListOf<Float>()
        val gridSize = 10
        val step = 1.0f

        for (i in -gridSize..gridSize) {
            // Lines along Z
            gridLines.add(i * step); gridLines.add(0f); gridLines.add(-gridSize * step)
            gridLines.add(i * step); gridLines.add(0f); gridLines.add(gridSize * step)
            // Lines along X
            gridLines.add(-gridSize * step); gridLines.add(0f); gridLines.add(i * step)
            gridLines.add(gridSize * step); gridLines.add(0f); gridLines.add(i * step)
        }

        val gridBuffer = createFloatBuffer(gridLines.toFloatArray())

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(lineMvpHandle, 1, false, mvpMatrix, 0)

        GLES20.glEnableVertexAttribArray(linePosHandle)
        GLES20.glVertexAttribPointer(linePosHandle, 3, GLES20.GL_FLOAT, false, 12, gridBuffer)

        // Draw Grid Lines in muted grey
        GLES20.glUniform4f(lineColorHandle, 0.22f, 0.25f, 0.3f, 1.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, gridLines.size / 3)

        // Draw Origin Axes: X (Red), Y (Green), Z (Blue)
        val axesCoords = floatArrayOf(
            0f, 0f, 0f, 2.5f, 0f, 0f, // X
            0f, 0f, 0f, 0f, 2.5f, 0f, // Y
            0f, 0f, 0f, 0f, 0f, 2.5f  // Z
        )
        val axesBuffer = createFloatBuffer(axesCoords)
        GLES20.glVertexAttribPointer(linePosHandle, 3, GLES20.GL_FLOAT, false, 12, axesBuffer)

        GLES20.glLineWidth(3.0f)
        GLES20.glUniform4f(lineColorHandle, 0.93f, 0.27f, 0.27f, 1f) // Red X
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)

        GLES20.glUniform4f(lineColorHandle, 0.06f, 0.72f, 0.5f, 1f) // Green Y
        GLES20.glDrawArrays(GLES20.GL_LINES, 2, 2)

        GLES20.glUniform4f(lineColorHandle, 0.23f, 0.51f, 0.96f, 1f) // Blue Z
        GLES20.glDrawArrays(GLES20.GL_LINES, 4, 2)

        GLES20.glDisableVertexAttribArray(linePosHandle)
    }

    private fun drawMesh(obj: com.blendroid.app.engine.model.SceneObject, shadingMode: ShadingMode, isSelected: Boolean) {
        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        var indexOffset: Short = 0
        for (face in obj.mesh.faces) {
            if (face.vertexIndices.size < 3) continue

            val idx0 = face.vertexIndices[0]
            val idx1 = face.vertexIndices[1]
            val idx2 = face.vertexIndices[2]

            val v0 = obj.mesh.vertices.getOrNull(idx0)?.position ?: Vector3.Zero
            val v1 = obj.mesh.vertices.getOrNull(idx1)?.position ?: Vector3.Zero
            val v2 = obj.mesh.vertices.getOrNull(idx2)?.position ?: Vector3.Zero

            val normal = (v1 - v0).cross(v2 - v0).normalized()

            // First Triangle
            vertices.add(v0.x); vertices.add(v0.y); vertices.add(v0.z)
            normals.add(normal.x); normals.add(normal.y); normals.add(normal.z)

            vertices.add(v1.x); vertices.add(v1.y); vertices.add(v1.z)
            normals.add(normal.x); normals.add(normal.y); normals.add(normal.z)

            vertices.add(v2.x); vertices.add(v2.y); vertices.add(v2.z)
            normals.add(normal.x); normals.add(normal.y); normals.add(normal.z)

            indices.add(indexOffset); indices.add((indexOffset + 1).toShort()); indices.add((indexOffset + 2).toShort())
            indexOffset = (indexOffset + 3).toShort()

            // Quad face second triangle
            if (face.vertexIndices.size >= 4) {
                val idx3 = face.vertexIndices[3]
                val v3 = obj.mesh.vertices.getOrNull(idx3)?.position ?: Vector3.Zero

                vertices.add(v0.x); vertices.add(v0.y); vertices.add(v0.z)
                normals.add(normal.x); normals.add(normal.y); normals.add(normal.z)

                vertices.add(v2.x); vertices.add(v2.y); vertices.add(v2.z)
                normals.add(normal.x); normals.add(normal.y); normals.add(normal.z)

                vertices.add(v3.x); vertices.add(v3.y); vertices.add(v3.z)
                normals.add(normal.x); normals.add(normal.y); normals.add(normal.z)

                indices.add(indexOffset); indices.add((indexOffset + 1).toShort()); indices.add((indexOffset + 2).toShort())
                indexOffset = (indexOffset + 3).toShort()
            }
        }

        if (vertices.isEmpty()) return

        val vertBuffer = createFloatBuffer(vertices.toFloatArray())
        val normBuffer = createFloatBuffer(normals.toFloatArray())
        val indexBuffer = createShortBuffer(indices.toShortArray())

        GLES20.glUseProgram(shaderProgram)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertBuffer)

        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 12, normBuffer)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)
        GLES20.glUniform3f(lightPosHandle, 10f, 15f, 10f)

        val modeInt = when (shadingMode) {
            ShadingMode.WIREFRAME -> 0
            ShadingMode.SOLID -> 1
            ShadingMode.MATERIAL -> 2
            ShadingMode.RENDERED -> 3
        }
        GLES20.glUniform1i(shadingModeHandle, modeInt)

        val matColor = obj.material.baseColor
        val colorR = if (isSelected) 0.97f else matColor.r
        val colorG = if (isSelected) 0.58f else matColor.g
        val colorB = if (isSelected) 0.11f else matColor.b

        GLES20.glUniform4f(colorHandle, colorR, colorG, colorB, matColor.a)

        if (shadingMode != ShadingMode.WIREFRAME) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        }

        // Draw Wireframe Overlay
        GLES20.glUniform1i(shadingModeHandle, 0)
        val outlineR = if (isSelected) 0.98f else 0.4f
        val outlineG = if (isSelected) 0.45f else 0.45f
        val outlineB = if (isSelected) 0.09f else 0.5f
        GLES20.glUniform4f(colorHandle, outlineR, outlineG, outlineB, 1.0f)
        GLES20.glLineWidth(if (isSelected) 2.5f else 1.0f)

        for (i in 0 until indices.size step 3) {
            GLES20.glDrawElements(GLES20.GL_LINE_LOOP, 3, GLES20.GL_UNSIGNED_SHORT, createShortBuffer(shortArrayOf(indices[i], indices[i + 1], indices[i + 2])))
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }

    private fun drawSimplePointGizmo(matrix: FloatArray, color: FloatArray) {
        GLES20.glUseProgram(lineProgram)
        val fullMvp = FloatArray(16)
        Matrix.multiplyMM(fullMvp, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(fullMvp, 0, fullMvp, 0, matrix, 0)

        GLES20.glUniformMatrix4fv(lineMvpHandle, 1, false, fullMvp, 0)
        GLES20.glUniform4f(lineColorHandle, color[0], color[1], color[2], color[3])

        val pointVerts = floatArrayOf(0f, 0f, 0f)
        val buf = createFloatBuffer(pointVerts)
        GLES20.glEnableVertexAttribArray(linePosHandle)
        GLES20.glVertexAttribPointer(linePosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
        GLES20.glLineWidth(8f)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
        GLES20.glDisableVertexAttribArray(linePosHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun createFloatBuffer(array: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(array.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(array)
                position(0)
            }
        }
    }

    private fun createShortBuffer(array: ShortArray): ShortBuffer {
        return ByteBuffer.allocateDirect(array.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(array)
                position(0)
            }
        }
    }
}

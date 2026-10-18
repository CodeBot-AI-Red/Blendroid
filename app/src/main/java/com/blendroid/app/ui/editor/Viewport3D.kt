package com.blendroid.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blendroid.app.engine.model.EditorMode
import com.blendroid.app.engine.model.MeshPrimitiveType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.ShadingMode
import com.blendroid.app.engine.model.Vector3
import com.blendroid.app.ui.theme.DarkBackground
import com.blendroid.app.ui.theme.PrimaryIndigo
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Viewport3D(
    scene: SceneGraph,
    selectedGizmoMode: String, // "MOVE", "ROTATE", "SCALE"
    onSelectObject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Camera Orbit State
    var yaw by remember { mutableStateOf(35f) }
    var pitch by remember { mutableStateOf(25f) }
    var zoom by remember { mutableStateOf(35f) }
    var panX by remember { mutableStateOf(0f) }
    var panY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomFactor, _ ->
                    yaw += pan.x * 0.4f
                    pitch = (pitch - pan.y * 0.4f).coerceIn(-85f, 85f)
                    zoom = (zoom / zoomFactor).coerceIn(10f, 150f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f + panX, size.height / 2f + panY)

            // Convert Camera Euler Orbit to radians
            val radYaw = Math.toRadians(yaw.toDouble()).toFloat()
            val radPitch = Math.toRadians(pitch.toDouble()).toFloat()

            fun project3DTo2D(v: Vector3): Offset {
                // Apply Orbit Yaw and Pitch transform to 3D point
                var x = v.x
                var y = v.y
                var z = v.z

                // Orbit Yaw (around Y axis)
                val cosY = cos(radYaw)
                val sinY = sin(radYaw)
                val x1 = x * cosY + z * sinY
                val z1 = -x * sinY + z * cosY

                // Orbit Pitch (around X axis)
                val cosP = cos(radPitch)
                val sinP = sin(radPitch)
                val y2 = y * cosP - z1 * sinP
                val z2 = y * sinP + z1 * cosP

                // Perspective projection mapping scale
                val fovScale = zoom * (size.height / 600f)
                val depthFactor = 1.0f / (1.0f + (z2 / 50f))

                val screenX = center.x + x1 * fovScale * depthFactor
                val screenY = center.y - y2 * fovScale * depthFactor

                return Offset(screenX, screenY)
            }

            // Draw 3D Grid Floor
            val gridSize = 6
            val gridStep = 1.0f
            val gridColor = Color(0xFF2E333D)

            for (i in -gridSize..gridSize) {
                val p1 = project3DTo2D(Vector3(i * gridStep, 0f, -gridSize * gridStep))
                val p2 = project3DTo2D(Vector3(i * gridStep, 0f, gridSize * gridStep))
                drawLine(gridColor, p1, p2, strokeWidth = 1f)

                val p3 = project3DTo2D(Vector3(-gridSize * gridStep, 0f, i * gridStep))
                val p4 = project3DTo2D(Vector3(gridSize * gridStep, 0f, i * gridStep))
                drawLine(gridColor, p3, p4, strokeWidth = 1f)
            }

            // Draw X, Y, Z Axis Lines from Origin
            val origin = project3DTo2D(Vector3.Zero)
            val xAxis = project3DTo2D(Vector3(2.5f, 0f, 0f))
            val yAxis = project3DTo2D(Vector3(0f, 2.5f, 0f))
            val zAxis = project3DTo2D(Vector3(0f, 0f, 2.5f))

            drawLine(Color(0xFFEF4444), origin, xAxis, strokeWidth = 2.5f) // X Axis - Red
            drawLine(Color(0xFF10B981), origin, yAxis, strokeWidth = 2.5f) // Y Axis - Green
            drawLine(Color(0xFF3B82F6), origin, zAxis, strokeWidth = 2.5f) // Z Axis - Blue

            // Draw Scene Objects
            for (obj in scene.objects) {
                if (!obj.isVisible) continue

                val isSelected = obj.id == scene.activeObjectId

                when (obj.type) {
                    MeshPrimitiveType.LIGHT -> {
                        val pos2d = project3DTo2D(obj.transform.position)
                        drawCircle(Color(0xFFF59E0B), radius = 10f, center = pos2d)
                        drawCircle(Color(0xFFFCD34D), radius = 16f, center = pos2d, style = Stroke(width = 2f))
                    }

                    MeshPrimitiveType.CAMERA -> {
                        val pos2d = project3DTo2D(obj.transform.position)
                        drawCircle(Color(0xFF9333EA), radius = 10f, center = pos2d)
                        drawCircle(Color.White, radius = 14f, center = pos2d, style = Stroke(width = 1.5f))
                    }

                    MeshPrimitiveType.ARMATURE -> {
                        obj.armature?.bones?.forEach { bone ->
                            val head2d = project3DTo2D(obj.transform.applyToPoint(bone.head))
                            val tail2d = project3DTo2D(obj.transform.applyToPoint(bone.tail))

                            val boneColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFFF87171)
                            drawLine(boneColor, head2d, tail2d, strokeWidth = 4f)
                            drawCircle(Color.White, radius = 5f, center = head2d)
                            drawCircle(Color.White, radius = 5f, center = tail2d)
                        }
                    }

                    else -> {
                        // Render Mesh Object
                        val verts = obj.mesh.vertices
                        val faces = obj.mesh.faces

                        if (verts.isNotEmpty() && faces.isNotEmpty()) {
                            val baseColor = if (scene.shadingMode == ShadingMode.WIREFRAME) {
                                Color.Transparent
                            } else {
                                Color(obj.material.baseColor.toArgbInt())
                            }

                            val outlineColor = if (isSelected) Color(0xFFF97316) else Color(0xFF64748B)

                            for (face in faces) {
                                if (face.vertexIndices.size < 3) continue

                                val path = Path()
                                val p0 = project3DTo2D(obj.transform.applyToPoint(verts[face.vertexIndices[0]].position))
                                path.moveTo(p0.x, p0.y)

                                for (i in 1 until face.vertexIndices.size) {
                                    val pi = project3DTo2D(obj.transform.applyToPoint(verts[face.vertexIndices[i]].position))
                                    path.lineTo(pi.x, pi.y)
                                }
                                path.close()

                                // Fill face if not Wireframe
                                if (scene.shadingMode != ShadingMode.WIREFRAME) {
                                    drawPath(path, baseColor.copy(alpha = if (scene.shadingMode == ShadingMode.MATERIAL || scene.shadingMode == ShadingMode.RENDERED) 0.9f else 0.7f))
                                }

                                // Stroke Wireframe / Selection Edge
                                drawPath(
                                    path = path,
                                    color = if (face.isSelected && scene.editorMode == EditorMode.EDIT_MODE) Color(0xFFEF4444) else outlineColor,
                                    style = Stroke(width = if (isSelected) 2.5f else 1f)
                                )
                            }
                        }
                    }
                }

                // Draw Object Center Gizmo if selected
                if (isSelected) {
                    val objCenter2d = project3DTo2D(obj.transform.position)
                    drawCircle(Color(0xFFF97316), radius = 6f, center = objCenter2d)

                    val gx = project3DTo2D(obj.transform.position + Vector3(1.2f, 0f, 0f))
                    val gy = project3DTo2D(obj.transform.position + Vector3(0f, 1.2f, 0f))
                    val gz = project3DTo2D(obj.transform.position + Vector3(0f, 0f, 1.2f))

                    drawLine(Color(0xFFEF4444), objCenter2d, gx, strokeWidth = 3f)
                    drawLine(Color(0xFF10B981), objCenter2d, gy, strokeWidth = 3f)
                    drawLine(Color(0xFF3B82F6), objCenter2d, gz, strokeWidth = 3f)
                }
            }
        }

        // Viewport Overlay Info Badge (Top Left)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val activeName = scene.getActiveObject()?.name ?: "Nenhum Selecionado"
            Text(
                text = "3D Viewport | $activeName | Yaw: ${yaw.toInt()}° Pitch: ${pitch.toInt()}°",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White
            )
        }
    }
}

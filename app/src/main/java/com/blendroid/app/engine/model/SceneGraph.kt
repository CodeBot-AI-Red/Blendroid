package com.blendroid.app.engine.model

import java.util.UUID

data class MaterialData(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Material",
    var baseColor: Color4 = Color4.DefaultGrey,
    var metallic: Float = 0.0f,
    var roughness: Float = 0.5f,
    var emissionColor: Color4 = Color4(0f, 0f, 0f, 1f),
    var alpha: Float = 1.0f
)

enum class EditorMode {
    OBJECT_MODE, EDIT_MODE, SCULPT_MODE, POSE_MODE, VERTEX_PAINT, WEIGHT_PAINT, TEXTURE_PAINT
}

fun EditorMode.getLabel(): String {
    return when (this) {
        EditorMode.OBJECT_MODE -> "Object Mode"
        EditorMode.EDIT_MODE -> "Edit Mode"
        EditorMode.SCULPT_MODE -> "Sculpt Mode"
        EditorMode.POSE_MODE -> "Pose Mode"
        EditorMode.VERTEX_PAINT -> "Vertex Paint"
        EditorMode.WEIGHT_PAINT -> "Weight Paint"
        EditorMode.TEXTURE_PAINT -> "Texture Paint"
    }
}

enum class ShadingMode {
    WIREFRAME, SOLID, MATERIAL, RENDERED
}

enum class EditSelectMode {
    VERTEX, EDGE, FACE
}

data class LightData(
    val type: String = "POINT", // POINT, SUN, SPOT
    var color: Color4 = Color4.White,
    var intensity: Float = 1000f
)

data class CameraData(
    var fov: Float = 50f,
    var near: Float = 0.1f,
    var far: Float = 1000f,
    var isOrthographic: Boolean = false
)

data class SceneObject(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var type: MeshPrimitiveType,
    var transform: Transform = Transform.Identity,
    var mesh: MeshData = MeshData(),
    var material: MaterialData = MaterialData(),
    val modifiers: MutableList<Modifier> = mutableListOf(),
    var armature: Armature? = null,
    var lightData: LightData? = null,
    var cameraData: CameraData? = null,
    var parentId: String? = null,
    var isVisible: Boolean = true,
    var isSelected: Boolean = false,
    var isSelectable: Boolean = true,
    var isRenderable: Boolean = true
)

data class SceneGraph(
    val objects: MutableList<SceneObject> = mutableListOf(),
    var activeObjectId: String? = null,
    var currentFrame: Int = 1,
    var totalFrames: Int = 250,
    var startFrame: Int = 1,
    var endFrame: Int = 250,
    var isPlaying: Boolean = false,
    var isAutoKeying: Boolean = false,
    var animationTrack: AnimationTrack = AnimationTrack(),
    var editorMode: EditorMode = EditorMode.OBJECT_MODE,
    var shadingMode: ShadingMode = ShadingMode.SOLID,
    var editSelectMode: EditSelectMode = EditSelectMode.FACE,

    // Mobile & Blender 3D Viewport Options
    var cursorPosition: Vector3 = Vector3(0f, 0f, 0f),
    var transformOrientation: String = "GLOBAL", // GLOBAL, LOCAL, NORMAL, GIMBAL, VIEW, CURSOR
    var pivotPoint: String = "MEDIAN_POINT", // BOUNDING_BOX, 3D_CURSOR, INDIVIDUAL_ORIGINS, MEDIAN_POINT, ACTIVE_ELEMENT
    var snapEnabled: Boolean = false,
    var snapType: String = "INCREMENT", // INCREMENT, VERTEX, EDGE, FACE, VOLUME
    var proportionalEditing: Boolean = false,
    var proportionalFalloff: String = "SMOOTH", // SMOOTH, SPHERE, ROOT, SHARP, LINEAR, CONSTANT, RANDOM
    var showOverlays: Boolean = true,
    var showXRay: Boolean = false,
    var activeWorkspace: String = "Layout", // Layout, Modeling, Sculpting, Animation, Rendering, Geometry Nodes
    var activeTool: String = "SELECT", // SELECT, MOVE, ROTATE, SCALE, ANNOTATE, MEASURE, ADD, EXTRUDE, INSET, BEVEL, LOOP_CUT, SCULPT_BRUSH
    var selectMode: String = "BOX", // BOX, CIRCLE, LASSO
    var sculptBrush: String = "DRAW" // DRAW, CLAY, SMOOTH, GRAB, INFLATE, PINCH
) {
    val undoStack: MutableList<SceneGraphSnapshot> = mutableListOf()
    val redoStack: MutableList<SceneGraphSnapshot> = mutableListOf()

    fun getActiveObject(): SceneObject? = objects.find { it.id == activeObjectId }

    fun addCube(name: String = "Cube"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.CUBE,
            mesh = MeshData.createCube()
        )
        objects.add(obj)
        activeObjectId = obj.id
        return obj
    }

    fun addSphere(name: String = "Sphere"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.SPHERE,
            mesh = MeshData.createSphere()
        )
        objects.add(obj)
        activeObjectId = obj.id
        return obj
    }

    fun addCylinder(name: String = "Cylinder"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.CYLINDER,
            mesh = MeshData.createCylinder()
        )
        objects.add(obj)
        activeObjectId = obj.id
        return obj
    }

    fun addPlane(name: String = "Plane"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.PLANE,
            mesh = MeshData.createPlane()
        )
        objects.add(obj)
        activeObjectId = obj.id
        return obj
    }

    fun addTorus(name: String = "Torus"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.TORUS,
            mesh = MeshData.createTorus()
        )
        objects.add(obj)
        activeObjectId = obj.id
        return obj
    }

    fun addMonkey(name: String = "Suzanne"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.MONKEY,
            mesh = MeshData.createMonkey()
        )
        objects.add(obj)
        activeObjectId = obj.id
        return obj
    }

    fun addArmature(name: String = "Armature"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.ARMATURE,
            armature = Armature.createHumanoidRig()
        )
        objects.add(obj)
        activeObjectId = obj.id
        return obj
    }

    fun addLight(name: String = "Point Light"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.LIGHT,
            transform = Transform(position = Vector3(4f, 6f, 4f)),
            lightData = LightData()
        )
        objects.add(obj)
        return obj
    }

    fun addCamera(name: String = "Camera"): SceneObject {
        val obj = SceneObject(
            name = name,
            type = MeshPrimitiveType.CAMERA,
            transform = Transform(position = Vector3(0f, 2f, 7f)),
            cameraData = CameraData()
        )
        objects.add(obj)
        return obj
    }

    fun deleteObject(id: String) {
        objects.removeIf { it.id == id }
        if (activeObjectId == id) {
            activeObjectId = objects.firstOrNull()?.id
        }
    }

    fun duplicateActiveObject(): SceneObject? {
        val active = getActiveObject() ?: return null
        val dup = active.copy(
            id = UUID.randomUUID().toString(),
            name = "${active.name}.001",
            transform = active.transform.copy(
                position = active.transform.position + Vector3(1f, 0f, 0f)
            ),
            mesh = active.mesh.clone()
        )
        objects.add(dup)
        activeObjectId = dup.id
        return dup
    }

    fun pushUndoSnapshot() {
        if (undoStack.size >= 30) {
            undoStack.removeAt(0)
        }
        undoStack.add(createSnapshot())
        redoStack.clear()
    }

    fun createSnapshot(): SceneGraphSnapshot {
        return SceneGraphSnapshot(
            objects = objects.map { it.copy(mesh = it.mesh.clone(), modifiers = it.modifiers.toMutableList()) }.toMutableList(),
            activeObjectId = activeObjectId,
            currentFrame = currentFrame,
            editorMode = editorMode,
            shadingMode = shadingMode,
            cursorPosition = cursorPosition
        )
    }

    fun restoreSnapshot(snapshot: SceneGraphSnapshot) {
        objects.clear()
        objects.addAll(snapshot.objects.map { it.copy(mesh = it.mesh.clone(), modifiers = it.modifiers.toMutableList()) })
        activeObjectId = snapshot.activeObjectId
        currentFrame = snapshot.currentFrame
        editorMode = snapshot.editorMode
        shadingMode = snapshot.shadingMode
        cursorPosition = snapshot.cursorPosition
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        val currentSnap = createSnapshot()
        redoStack.add(currentSnap)

        val lastSnap = undoStack.removeAt(undoStack.size - 1)
        restoreSnapshot(lastSnap)
        return true
    }

    fun redo(): Boolean {
        if (redoStack.isEmpty()) return false
        val currentSnap = createSnapshot()
        undoStack.add(currentSnap)

        val nextSnap = redoStack.removeAt(redoStack.size - 1)
        restoreSnapshot(nextSnap)
        return true
    }

    companion object {
        fun createDefaultScene(): SceneGraph {
            val scene = SceneGraph()
            scene.addCamera()
            scene.addLight()
            val cube = scene.addCube()
            cube.isSelected = true
            scene.activeObjectId = cube.id
            return scene
        }
    }
}

data class SceneGraphSnapshot(
    val objects: List<SceneObject>,
    val activeObjectId: String?,
    val currentFrame: Int,
    val editorMode: EditorMode,
    val shadingMode: ShadingMode,
    val cursorPosition: Vector3
)

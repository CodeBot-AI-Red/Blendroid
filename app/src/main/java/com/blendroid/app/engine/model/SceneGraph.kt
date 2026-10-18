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
    OBJECT_MODE, EDIT_MODE, SCULPT_MODE, POSE_MODE
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
    var isSelected: Boolean = false
)

data class SceneGraph(
    val objects: MutableList<SceneObject> = mutableListOf(),
    var activeObjectId: String? = null,
    var currentFrame: Int = 1,
    var totalFrames: Int = 250,
    var isPlaying: Boolean = false,
    var animationTrack: AnimationTrack = AnimationTrack(),
    var editorMode: EditorMode = EditorMode.OBJECT_MODE,
    var shadingMode: ShadingMode = ShadingMode.SOLID,
    var editSelectMode: EditSelectMode = EditSelectMode.FACE
) {
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

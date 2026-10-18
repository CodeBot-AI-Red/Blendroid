package com.blendroid.app.engine.ai

import com.blendroid.app.engine.model.Color4
import com.blendroid.app.engine.model.Keyframe
import com.blendroid.app.engine.model.Modifier
import com.blendroid.app.engine.model.ModifierType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.Transform
import com.blendroid.app.engine.model.Vector3

class AiCommandExecutor {

    fun createPrimitive(scene: SceneGraph, type: String, name: String): String {
        val createdObj = when (type.lowercase()) {
            "cube" -> scene.addCube(name)
            "sphere" -> scene.addSphere(name)
            "cylinder" -> scene.addCylinder(name)
            "plane" -> scene.addPlane(name)
            "torus" -> scene.addTorus(name)
            "monkey", "suzanne" -> scene.addMonkey(name)
            "armature", "rig" -> scene.addArmature(name)
            "light" -> scene.addLight(name)
            "camera" -> scene.addCamera(name)
            else -> scene.addCube(name)
        }
        return "Objeto 3D '${createdObj.name}' (${createdObj.type}) criado com sucesso na cena."
    }

    fun transformObject(obj: com.blendroid.app.engine.model.SceneObject, pos: Vector3, rot: Vector3, scale: Vector3) {
        obj.transform = Transform(position = pos, rotation = rot, scale = scale)
    }

    fun setMaterial(scene: SceneGraph, colorHex: String, metallic: Float, roughness: Float): String {
        val active = scene.getActiveObject() ?: return "Nenhum objeto selecionado."
        val color = try {
            val hex = colorHex.replace("#", "")
            val r = Integer.parseInt(hex.substring(0, 2), 16) / 255f
            val g = Integer.parseInt(hex.substring(2, 4), 16) / 255f
            val b = Integer.parseInt(hex.substring(4, 6), 16) / 255f
            Color4(r, g, b, 1f)
        } catch (e: Exception) {
            Color4.DefaultOrange
        }

        active.material = active.material.copy(
            baseColor = color,
            metallic = metallic,
            roughness = roughness
        )
        return "Material de '${active.name}' atualizado (Cor: $colorHex, Metálico: $metallic, Rugosidade: $roughness)."
    }

    fun extrudeSelectedFaces(scene: SceneGraph, distance: Float): String {
        val active = scene.getActiveObject() ?: return "Nenhum objeto selecionado."
        active.mesh.extrudeSelectedFaces(distance)
        return "Faces do objeto '${active.name}' extrudadas em $distance unidades."
    }

    fun subdivideActiveMesh(scene: SceneGraph): String {
        val active = scene.getActiveObject() ?: return "Nenhum objeto selecionado."
        active.mesh.subdivide()
        return "Malha de '${active.name}' subdividida (Total vértices: ${active.mesh.vertices.size})."
    }

    fun addModifier(scene: SceneGraph, modifierType: ModifierType): String {
        val active = scene.getActiveObject() ?: return "Nenhum objeto selecionado."
        val modName = modifierType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        val modifier = Modifier(
            id = System.currentTimeMillis().toString(),
            name = modName,
            type = modifierType
        )
        active.modifiers.add(modifier)
        return "Modificador '$modName' adicionado ao objeto '${active.name}'."
    }

    fun addKeyframe(scene: SceneGraph, frame: Int): String {
        val active = scene.getActiveObject() ?: return "Nenhum objeto selecionado."
        val boneTransforms = mutableMapOf<String, Transform>()
        active.armature?.bones?.forEach { b ->
            boneTransforms[b.id] = b.transform
        }

        val kf = Keyframe(
            frame = frame,
            transform = active.transform,
            boneTransforms = boneTransforms
        )
        scene.animationTrack.keyframes.removeAll { it.frame == frame }
        scene.animationTrack.keyframes.add(kf)
        return "Keyframe adicionado no frame $frame para '${active.name}'."
    }

    fun generateProceduralScene(scene: SceneGraph, preset: String): String {
        scene.objects.clear()
        scene.addCamera()
        scene.addLight()

        when (preset.lowercase()) {
            "city" -> {
                for (x in -2..2) {
                    for (z in -2..2) {
                        val h = ((x * x + z * z) % 3 + 1) * 1.5f
                        val b = scene.addCube("Building_${x}_$z")
                        b.transform = Transform(
                            position = Vector3(x * 2.5f, h / 2f, z * 2.5f),
                            scale = Vector3(1.8f, h, 1.8f)
                        )
                        val blueShade = 0.3f + (x + 2) * 0.15f
                        b.material = b.material.copy(baseColor = Color4(0.2f, 0.4f, blueShade, 1f))
                    }
                }
                val road = scene.addPlane("CityGround")
                road.transform = Transform(scale = Vector3(8f, 1f, 8f))
                road.material = road.material.copy(baseColor = Color4(0.15f, 0.15f, 0.18f, 1f))
                return "Cidade 3D gerada proceduralmente por IA com edifícios e plano de solo!"
            }

            "forest" -> {
                val ground = scene.addPlane("ForestGround")
                ground.transform = Transform(scale = Vector3(10f, 1f, 10f))
                ground.material = ground.material.copy(baseColor = Color4(0.15f, 0.5f, 0.2f, 1f))

                for (i in 0..12) {
                    val px = (Math.random() * 12 - 6).toFloat()
                    val pz = (Math.random() * 12 - 6).toFloat()

                    val trunk = scene.addCylinder("TreeTrunk_$i")
                    trunk.transform = Transform(
                        position = Vector3(px, 0.8f, pz),
                        scale = Vector3(0.3f, 1.6f, 0.3f)
                    )
                    trunk.material = trunk.material.copy(baseColor = Color4(0.4f, 0.25f, 0.1f, 1f))

                    val leaves = scene.addSphere("Leaves_$i")
                    leaves.transform = Transform(
                        position = Vector3(px, 2.2f, pz),
                        scale = Vector3(1.2f, 1.4f, 1.2f)
                    )
                    leaves.material = leaves.material.copy(baseColor = Color4(0.1f, 0.7f, 0.25f, 1f))
                }
                return "Floresta 3D gerada proceduralmente por IA!"
            }

            "character", "robot" -> {
                val head = scene.addSphere("Head")
                head.transform = Transform(position = Vector3(0f, 2.8f, 0f), scale = Vector3(0.8f, 0.8f, 0.8f))
                head.material = head.material.copy(baseColor = Color4(0.9f, 0.7f, 0.5f, 1f))

                val body = scene.addCube("Body")
                body.transform = Transform(position = Vector3(0f, 1.5f, 0f), scale = Vector3(1.2f, 1.6f, 0.8f))
                body.material = body.material.copy(baseColor = Color4(0.2f, 0.5f, 0.9f, 1f))

                val leftArm = scene.addCylinder("Arm_L")
                leftArm.transform = Transform(position = Vector3(-1.1f, 1.5f, 0f), rotation = Vector3(0f, 0f, 15f), scale = Vector3(0.3f, 1.2f, 0.3f))

                val rightArm = scene.addCylinder("Arm_R")
                rightArm.transform = Transform(position = Vector3(1.1f, 1.5f, 0f), rotation = Vector3(0f, 0f, -15f), scale = Vector3(0.3f, 1.2f, 0.3f))

                val leftLeg = scene.addCylinder("Leg_L")
                leftLeg.transform = Transform(position = Vector3(-0.4f, 0.4f, 0f), scale = Vector3(0.35f, 1.2f, 0.35f))

                val rightLeg = scene.addCylinder("Leg_R")
                rightLeg.transform = Transform(position = Vector3(0.4f, 0.4f, 0f), scale = Vector3(0.35f, 1.2f, 0.35f))

                val rig = scene.addArmature("CharacterArmature")
                rig.transform = Transform(position = Vector3(0f, 0f, 0f))

                return "Personagem/Robô 3D estilizado com Armature e Rigging gerado por IA!"
            }

            else -> {
                scene.addCube("DefaultCube")
                return "Cena padrão redefinida."
            }
        }
    }
}

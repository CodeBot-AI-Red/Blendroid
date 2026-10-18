package com.blendroid.app.engine.ai

import com.blendroid.app.engine.model.SceneGraph

class ScriptRunner(
    private val commandExecutor: AiCommandExecutor,
    private val mcpBridge: McpBridge
) {

    fun executeNaturalLanguageCommand(prompt: String, scene: SceneGraph): String {
        val p = prompt.lowercase().trim()

        return when {
            p.contains("cidade") || p.contains("city") -> commandExecutor.generateProceduralScene(scene, "city")
            p.contains("floresta") || p.contains("forest") || p.contains("árvore") -> commandExecutor.generateProceduralScene(scene, "forest")
            p.contains("personagem") || p.contains("robô") || p.contains("robot") || p.contains("character") -> commandExecutor.generateProceduralScene(scene, "character")

            p.contains("criar cubo") || p.contains("add cube") -> commandExecutor.createPrimitive(scene, "cube", "Cube")
            p.contains("criar esfera") || p.contains("add sphere") -> commandExecutor.createPrimitive(scene, "sphere", "Sphere")
            p.contains("criar cilindro") || p.contains("add cylinder") -> commandExecutor.createPrimitive(scene, "cylinder", "Cylinder")
            p.contains("criar plano") || p.contains("add plane") -> commandExecutor.createPrimitive(scene, "plane", "Plane")
            p.contains("criar macaco") || p.contains("suzanne") -> commandExecutor.createPrimitive(scene, "monkey", "Suzanne")

            p.contains("extrud") -> commandExecutor.extrudeSelectedFaces(scene, 0.8f)
            p.contains("subdiv") -> commandExecutor.subdivideActiveMesh(scene)
            p.contains("keyframe") || p.contains("animar") || p.contains("animate") -> {
                val f1 = commandExecutor.addKeyframe(scene, scene.currentFrame)
                val f2 = commandExecutor.addKeyframe(scene, scene.currentFrame + 30)
                "$f1 $f2"
            }

            p.contains("vermelho") || p.contains("red") -> commandExecutor.setMaterial(scene, "#EF4444", 0.3f, 0.5f)
            p.contains("azul") || p.contains("blue") -> commandExecutor.setMaterial(scene, "#3B82F6", 0.5f, 0.3f)
            p.contains("verde") || p.contains("green") -> commandExecutor.setMaterial(scene, "#10B981", 0.2f, 0.4f)
            p.contains("ouro") || p.contains("dourado") || p.contains("gold") -> commandExecutor.setMaterial(scene, "#F59E0B", 0.95f, 0.15f)

            else -> {
                // Fallback: create primitive or default transform action
                commandExecutor.createPrimitive(scene, "cube", "Obj_AI")
            }
        }
    }
}

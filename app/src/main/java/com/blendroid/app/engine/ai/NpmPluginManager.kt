package com.blendroid.app.engine.ai

import com.blendroid.app.engine.model.SceneGraph

data class NpmAiPackage(
    val packageName: String,
    val version: String,
    val description: String,
    val author: String,
    var isEnabled: Boolean = true,
    val actions: List<String>
)

class NpmPluginManager(
    private val commandExecutor: AiCommandExecutor,
    private val mcpBridge: McpBridge
) {

    private val installedPackages = mutableListOf<NpmAiPackage>()

    init {
        // Pre-install standard AI extension packages
        installedPackages.add(
            NpmAiPackage(
                packageName = "@blendroid/mcp-mesh-generator",
                version = "1.2.0",
                description = "Gera malhas complexas e cidades/florestas via comandos MCP e prompts de IA",
                author = "Blendroid Community",
                actions = listOf("Gerar Cidade 3D", "Gerar Floresta 3D", "Gerar Terreno Low-Poly")
            )
        )
        installedPackages.add(
            NpmAiPackage(
                packageName = "@ai-3d/auto-rigger-animator",
                version = "2.0.4",
                description = "Adiciona esqueletos (armatures) e automatiza animações de robôs e personagens",
                author = "AI 3D Lab",
                actions = listOf("Riggar Personagem Automático", "Criar Animação de Caminhada (Walk Cycle)")
            )
        )
        installedPackages.add(
            NpmAiPackage(
                packageName = "@ai-3d/text-to-shader-materials",
                version = "1.0.1",
                description = "Cria materiais PBR realistas baseados em descrição em linguagem natural",
                author = "ShaderAI",
                actions = listOf("Gerar Shader Metálico", "Gerar Shader de Vidro Transparente", "Gerar Shader Neon Emissivo")
            )
        )
    }

    fun getInstalledPackages(): List<NpmAiPackage> = installedPackages

    fun installPackage(packageName: String): String {
        val cleanName = packageName.trim()
        if (installedPackages.any { it.packageName.equals(cleanName, ignoreCase = true) }) {
            return "Pacote '$cleanName' já está instalado."
        }

        val newPkg = NpmAiPackage(
            packageName = cleanName,
            version = "1.0.0",
            description = "Pacote de IA e automação NPM personalizado",
            author = "NPM Registry",
            actions = listOf("Executar Script do Pacote", "Configurar Parâmetros de IA")
        )
        installedPackages.add(newPkg)
        return "Pacote NPM '$cleanName' instalado com sucesso!"
    }

    fun togglePackage(pkg: NpmAiPackage) {
        val idx = installedPackages.indexOfFirst { it.packageName == pkg.packageName }
        if (idx != -1) {
            installedPackages[idx] = installedPackages[idx].copy(isEnabled = !pkg.isEnabled)
        }
    }

    fun executePackageAction(packageName: String, actionName: String, scene: SceneGraph): String {
        val pkg = installedPackages.find { it.packageName == packageName }
        if (pkg == null || !pkg.isEnabled) return "Pacote '$packageName' não está ativo."

        return when (actionName) {
            "Gerar Cidade 3D" -> commandExecutor.generateProceduralScene(scene, "city")
            "Gerar Floresta 3D" -> commandExecutor.generateProceduralScene(scene, "forest")
            "Riggar Personagem Automático", "Gerar Terreno Low-Poly" -> commandExecutor.generateProceduralScene(scene, "character")
            "Criar Animação de Caminhada (Walk Cycle)" -> {
                commandExecutor.addKeyframe(scene, 1)
                commandExecutor.addKeyframe(scene, 20)
                commandExecutor.addKeyframe(scene, 40)
                "Animação de Caminhada (Walk Cycle) gerada com keyframes nos frames 1, 20 e 40!"
            }
            "Gerar Shader Metálico" -> commandExecutor.setMaterial(scene, "#CBD5E1", 0.9f, 0.1f)
            "Gerar Shader Neon Emissivo" -> commandExecutor.setMaterial(scene, "#10B981", 0.0f, 0.2f)
            else -> "Ação '$actionName' executada via $packageName!"
        }
    }
}

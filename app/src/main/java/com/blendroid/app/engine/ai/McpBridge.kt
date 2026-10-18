package com.blendroid.app.engine.ai

import com.blendroid.app.engine.model.Color4
import com.blendroid.app.engine.model.ModifierType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.Vector3
import org.json.JSONArray
import org.json.JSONObject

data class McpServerConfig(
    val name: String,
    val serverUrl: String,
    var isConnected: Boolean = false,
    val statusText: String = "Desconectado"
)

data class McpTool(
    val name: String,
    val description: String,
    val parametersJsonSchema: String
)

class McpBridge(private val commandExecutor: AiCommandExecutor) {

    private val availableTools = mutableListOf<McpTool>()
    private val activeConnections = mutableListOf<McpServerConfig>()

    init {
        registerStandardTools()
        activeConnections.add(McpServerConfig("Local MCP AI Engine", "mcp://localhost:8080", isConnected = true, statusText = "Ativo"))
        activeConnections.add(McpServerConfig("Remote Anthropic/OpenAI MCP", "https://mcp.blendroid.ai/v1", isConnected = false, statusText = "Pronto para conectar"))
    }

    fun getTools(): List<McpTool> = availableTools

    fun getConnections(): List<McpServerConfig> = activeConnections

    fun toggleConnection(config: McpServerConfig) {
        val idx = activeConnections.indexOfFirst { it.serverUrl == config.serverUrl }
        if (idx != -1) {
            val curr = activeConnections[idx]
            val newConnected = !curr.isConnected
            val status = if (newConnected) "Ativo" else "Desconectado"
            activeConnections[idx] = curr.copy(isConnected = newConnected, statusText = status)
        }
    }

    private fun registerStandardTools() {
        availableTools.add(
            McpTool(
                "create_primitive",
                "Cria um primitivo 3D na cena (cube, sphere, cylinder, plane, torus, monkey, cone, armature, light, camera)",
                """{"type":"object","properties":{"primitive":{"type":"string"},"name":{"type":"string"}}}"""
            )
        )
        availableTools.add(
            McpTool(
                "transform_object",
                "Altera a transformação (posição, rotação, escala) do objeto ativo ou especificado",
                """{"type":"object","properties":{"objectName":{"type":"string"},"px":{"type":"number"},"py":{"type":"number"},"pz":{"type":"number"},"rx":{"type":"number"},"ry":{"type":"number"},"rz":{"type":"number"},"sx":{"type":"number"},"sy":{"type":"number"},"sz":{"type":"number"}}}"""
            )
        )
        availableTools.add(
            McpTool(
                "set_material",
                "Define as propriedades de material do objeto ativo (cor r,g,b, metallic, roughness)",
                """{"type":"object","properties":{"colorHex":{"type":"string"},"metallic":{"type":"number"},"roughness":{"type":"number"}}}"""
            )
        )
        availableTools.add(
            McpTool(
                "extrude_faces",
                "Extruda as faces selecionadas no modo de edição",
                """{"type":"object","properties":{"distance":{"type":"number"}}}"""
            )
        )
        availableTools.add(
            McpTool(
                "subdivide_mesh",
                "Aplica uma subdivisão geométrica na malha 3D selecionada",
                """{"type":"object","properties":{}}"""
            )
        )
        availableTools.add(
            McpTool(
                "add_modifier",
                "Adiciona um modificador (SUBDIVISION_SURFACE, MIRROR, ARRAY, SOLIDIFY) ao objeto",
                """{"type":"object","properties":{"type":{"type":"string"}}}"""
            )
        )
        availableTools.add(
            McpTool(
                "add_keyframe",
                "Insere um keyframe de animação no quadro atual",
                """{"type":"object","properties":{"frame":{"type":"number"}}}"""
            )
        )
        availableTools.add(
            McpTool(
                "generate_procedural_scene",
                "Gera automaticamente uma cena 3D por IA (ex: 'city', 'forest', 'character', 'landscape')",
                """{"type":"object","properties":{"preset":{"type":"string"}}}"""
            )
        )
        availableTools.add(
            McpTool(
                "export_model",
                "Exporta o modelo/cena para formato OBJ, STL, GLTF ou Blendroid",
                """{"type":"object","properties":{"format":{"type":"string"}}}"""
            )
        )
    }

    fun handleJsonRpcRequest(jsonRpcMessage: String, scene: SceneGraph): String {
        return try {
            val json = JSONObject(jsonRpcMessage)
            val id = json.opt("id") ?: 1
            val method = json.optString("method")
            val params = json.optJSONObject("params") ?: JSONObject()

            when (method) {
                "tools/list" -> {
                    val res = JSONObject()
                    res.put("jsonrpc", "2.0")
                    res.put("id", id)

                    val toolsArr = JSONArray()
                    for (t in availableTools) {
                        val tJ = JSONObject()
                        tJ.put("name", t.name)
                        tJ.put("description", t.description)
                        tJ.put("inputSchema", JSONObject(t.parametersJsonSchema))
                        toolsArr.put(tJ)
                    }

                    val resultObj = JSONObject()
                    resultObj.put("tools", toolsArr)
                    res.put("result", resultObj)
                    res.toString()
                }

                "tools/call" -> {
                    val toolName = params.optString("name")
                    val args = params.optJSONObject("arguments") ?: JSONObject()

                    val resultText = executeToolCall(toolName, args, scene)

                    val res = JSONObject()
                    res.put("jsonrpc", "2.0")
                    res.put("id", id)

                    val contentArr = JSONArray()
                    val cObj = JSONObject()
                    cObj.put("type", "text")
                    cObj.put("text", resultText)
                    contentArr.put(cObj)

                    val resultObj = JSONObject()
                    resultObj.put("content", contentArr)
                    res.put("result", resultObj)
                    res.toString()
                }

                else -> {
                    val err = JSONObject()
                    err.put("jsonrpc", "2.0")
                    err.put("id", id)

                    val errObj = JSONObject()
                    errObj.put("code", -32601)
                    errObj.put("message", "Método MCP não encontrado: $method")
                    err.put("error", errObj)
                    err.toString()
                }
            }
        } catch (e: Exception) {
            val err = JSONObject()
            err.put("jsonrpc", "2.0")
            err.put("id", 1)
            val errObj = JSONObject()
            errObj.put("code", -32603)
            errObj.put("message", "Erro ao processar requisição MCP: ${e.message}")
            err.put("error", errObj)
            err.toString()
        }
    }

    fun executeToolCall(toolName: String, args: JSONObject, scene: SceneGraph): String {
        return when (toolName) {
            "create_primitive" -> {
                val prim = args.optString("primitive", "cube").lowercase()
                val name = args.optString("name", prim.replaceFirstChar { it.uppercase() })
                commandExecutor.createPrimitive(scene, prim, name)
            }

            "transform_object" -> {
                val name = args.optString("objectName", "")
                val px = args.optDouble("px", 0.0).toFloat()
                val py = args.optDouble("py", 0.0).toFloat()
                val pz = args.optDouble("pz", 0.0).toFloat()
                val rx = args.optDouble("rx", 0.0).toFloat()
                val ry = args.optDouble("ry", 0.0).toFloat()
                val rz = args.optDouble("rz", 0.0).toFloat()
                val sx = args.optDouble("sx", 1.0).toFloat()
                val sy = args.optDouble("sy", 1.0).toFloat()
                val sz = args.optDouble("sz", 1.0).toFloat()

                val obj = if (name.isNotBlank()) scene.objects.find { it.name.equals(name, ignoreCase = true) } else scene.getActiveObject()
                if (obj != null) {
                    commandExecutor.transformObject(obj, Vector3(px, py, pz), Vector3(rx, ry, rz), Vector3(sx, sy, sz))
                    "Transformação aplicada ao objeto '${obj.name}'"
                } else {
                    "Nenhum objeto encontrado para transformar"
                }
            }

            "set_material" -> {
                val colorHex = args.optString("colorHex", "#3B82F6")
                val metallic = args.optDouble("metallic", 0.2).toFloat()
                val roughness = args.optDouble("roughness", 0.4).toFloat()
                commandExecutor.setMaterial(scene, colorHex, metallic, roughness)
            }

            "extrude_faces" -> {
                val dist = args.optDouble("distance", 0.5).toFloat()
                commandExecutor.extrudeSelectedFaces(scene, dist)
            }

            "subdivide_mesh" -> {
                commandExecutor.subdivideActiveMesh(scene)
            }

            "add_modifier" -> {
                val typeStr = args.optString("type", "SUBDIVISION_SURFACE").uppercase()
                val type = try { ModifierType.valueOf(typeStr) } catch (e: Exception) { ModifierType.SUBDIVISION_SURFACE }
                commandExecutor.addModifier(scene, type)
            }

            "add_keyframe" -> {
                val frame = args.optInt("frame", scene.currentFrame)
                commandExecutor.addKeyframe(scene, frame)
            }

            "generate_procedural_scene" -> {
                val preset = args.optString("preset", "city")
                commandExecutor.generateProceduralScene(scene, preset)
            }

            "export_model" -> {
                val format = args.optString("format", "obj")
                "Pronto para exportar no formato .$format"
            }

            else -> "Ferramenta desconhecida: $toolName"
        }
    }
}

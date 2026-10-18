package com.blendroid.app

import com.blendroid.app.engine.ai.AiCommandExecutor
import com.blendroid.app.engine.ai.McpBridge
import com.blendroid.app.engine.ai.NpmPluginManager
import com.blendroid.app.engine.ai.ScriptRunner
import com.blendroid.app.engine.export.BlendroidExporter
import com.blendroid.app.engine.export.GltfExporter
import com.blendroid.app.engine.export.ObjExporter
import com.blendroid.app.engine.export.StlExporter
import com.blendroid.app.engine.model.Color4
import com.blendroid.app.engine.model.MeshData
import com.blendroid.app.engine.model.ModifierType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.Transform
import com.blendroid.app.engine.model.Vector3
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {

    @Test
    fun test3DMathAndTransform() {
        val v1 = Vector3(1f, 2f, 3f)
        val v2 = Vector3(4f, 5f, 6f)
        val added = v1 + v2
        assertEquals(5f, added.x, 0.001f)
        assertEquals(7f, added.y, 0.001f)
        assertEquals(9f, added.z, 0.001f)

        val transform = Transform(position = Vector3(10f, 0f, 0f), scale = Vector3(2f, 2f, 2f))
        val p = Vector3(1f, 1f, 1f)
        val transformedP = transform.applyToPoint(p)
        assertEquals(12f, transformedP.x, 0.001f)
        assertEquals(2f, transformedP.y, 0.001f)
        assertEquals(2f, transformedP.z, 0.001f)
    }

    @Test
    fun testGeometryPrimitivesAndMeshOperations() {
        val cubeMesh = MeshData.createCube(1f)
        assertEquals(8, cubeMesh.vertices.size)
        assertEquals(6, cubeMesh.faces.size)

        // Test subdivision
        cubeMesh.subdivide()
        assertTrue(cubeMesh.faces.size > 6)

        val sphereMesh = MeshData.createSphere(1f, 6, 8)
        assertTrue(sphereMesh.vertices.isNotEmpty())
        assertTrue(sphereMesh.faces.isNotEmpty())
    }

    @Test
    fun testExporters() {
        val scene = SceneGraph.createDefaultScene()

        // OBJ Export
        val objStr = ObjExporter.exportToObj(scene)
        assertTrue(objStr.contains("o Cube"))
        assertTrue(objStr.contains("v "))

        // MTL Export
        val mtlStr = ObjExporter.exportToMtl(scene)
        assertTrue(mtlStr.contains("newmtl"))

        // STL Export
        val stlStr = StlExporter.exportToStlAscii(scene)
        assertTrue(stlStr.contains("solid blendroid_model"))
        assertTrue(stlStr.contains("endsolid blendroid_model"))

        // GLTF Export
        val gltfStr = GltfExporter.exportToGltfJson(scene)
        assertTrue(gltfStr.contains("\"generator\": \"Blendroid 3D Engine\""))

        // Blendroid JSON Export & Import
        val jsonStr = BlendroidExporter.exportToJson(scene)
        val restoredScene = BlendroidExporter.importFromJson(jsonStr)
        assertEquals(scene.objects.size, restoredScene.objects.size)
    }

    @Test
    fun testMcpAndAiScriptingEngine() {
        val executor = AiCommandExecutor()
        val mcpBridge = McpBridge(executor)
        val npmManager = NpmPluginManager(executor, mcpBridge)
        val runner = ScriptRunner(executor, mcpBridge)

        val scene = SceneGraph.createDefaultScene()

        // Test Natural Language Script Runner Commands
        val cityRes = runner.executeNaturalLanguageCommand("gerar cidade 3d", scene)
        assertTrue(cityRes.contains("Cidade 3D gerada"))
        assertTrue(scene.objects.size > 5)

        // Test MCP JSON-RPC call
        val jsonRpcReq = """
            {
                "jsonrpc": "2.0",
                "id": 100,
                "method": "tools/call",
                "params": {
                    "name": "create_primitive",
                    "arguments": {
                        "primitive": "torus",
                        "name": "AiTorus"
                    }
                }
            }
        """.trimIndent()

        val jsonRpcRes = mcpBridge.handleJsonRpcRequest(jsonRpcReq, scene)
        assertTrue(jsonRpcRes.contains("AiTorus"))

        // Test NPM Package Action
        val npmRes = npmManager.executePackageAction("@blendroid/mcp-mesh-generator", "Gerar Floresta 3D", scene)
        assertTrue(npmRes.contains("Floresta 3D gerada"))
    }
}

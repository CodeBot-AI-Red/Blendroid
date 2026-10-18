package com.blendroid.app.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.blendroid.app.engine.ai.AiCommandExecutor
import com.blendroid.app.engine.ai.McpBridge
import com.blendroid.app.engine.ai.NpmPluginManager
import com.blendroid.app.engine.ai.ScriptRunner
import com.blendroid.app.engine.export.BlendroidExporter
import com.blendroid.app.engine.export.GltfExporter
import com.blendroid.app.engine.export.ObjExporter
import com.blendroid.app.engine.export.ProjectStorageManager
import com.blendroid.app.engine.export.StlExporter
import com.blendroid.app.engine.export.StorageExportHelper
import com.blendroid.app.engine.model.EditorMode
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.ShadingMode
import com.blendroid.app.ui.theme.DarkBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    projectId: String,
    projectName: String,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load or create Scene Graph
    val scene = remember {
        val savedJson = ProjectStorageManager.loadProjectScene(context, projectId)
        if (!savedJson.isNullOrEmpty()) {
            try {
                BlendroidExporter.importFromJson(savedJson)
            } catch (e: Exception) {
                SceneGraph.createDefaultScene()
            }
        } else {
            SceneGraph.createDefaultScene()
        }
    }

    val commandExecutor = remember { AiCommandExecutor() }
    val mcpBridge = remember { McpBridge(commandExecutor) }
    val npmPluginManager = remember { NpmPluginManager(commandExecutor, mcpBridge) }
    val scriptRunner = remember { ScriptRunner(commandExecutor, mcpBridge) }

    var editorMode by remember { mutableStateOf(EditorMode.OBJECT_MODE) }
    var shadingMode by remember { mutableStateOf(ShadingMode.SOLID) }
    var showAiDrawer by remember { mutableStateOf(false) }

    var triggerRecompose by remember { mutableStateOf(0) }

    fun refreshUi() {
        triggerRecompose++
    }

    fun saveProjectLocally() {
        val json = BlendroidExporter.exportToJson(scene)
        ProjectStorageManager.saveProjectScene(context, projectId, json)
    }

    // Animation Loop
    LaunchedEffect(scene.isPlaying) {
        while (scene.isPlaying) {
            delay(1000L / 24L) // 24 FPS
            scene.currentFrame = if (scene.currentFrame >= scene.totalFrames) 1 else scene.currentFrame + 1

            // Evaluate animation track transform
            val activeObj = scene.getActiveObject()
            if (activeObj != null && scene.animationTrack.keyframes.isNotEmpty()) {
                activeObj.transform = scene.animationTrack.evaluateTransformAt(scene.currentFrame)
            }
            refreshUi()
        }
    }

    Scaffold(
        topBar = {
            EditorHeader(
                projectName = projectName,
                editorMode = editorMode,
                shadingMode = shadingMode,
                onModeChange = {
                    editorMode = it
                    scene.editorMode = it
                    refreshUi()
                },
                onShadingChange = {
                    shadingMode = it
                    scene.shadingMode = it
                    refreshUi()
                },
                onAddPrimitive = { primName ->
                    when (primName) {
                        "Cubo" -> scene.addCube()
                        "Esfera" -> scene.addSphere()
                        "Cilindro" -> scene.addCylinder()
                        "Plano" -> scene.addPlane()
                        "Torus" -> scene.addTorus()
                        "Suzanne (Macaco)" -> scene.addMonkey()
                        "Esqueleto (Armature)" -> scene.addArmature()
                        "Luz Ponto" -> scene.addLight()
                        "Câmera" -> scene.addCamera()
                    }
                    saveProjectLocally()
                    scope.launch { snackbarHostState.showSnackbar("Adicionado '$primName' à cena.") }
                    refreshUi()
                },
                onExport = { format ->
                    scope.launch {
                        val fileName = "${projectName.lowercase().replace(" ", "_")}_export.${format.lowercase()}"
                        val content = when (format) {
                            "OBJ" -> ObjExporter.exportToObj(scene)
                            "STL" -> StlExporter.exportToStlAscii(scene)
                            "GLTF" -> GltfExporter.exportToGltfJson(scene)
                            else -> BlendroidExporter.exportToJson(scene)
                        }
                        StorageExportHelper.saveToFile(context, fileName, content)
                        snackbarHostState.showSnackbar("Modelo exportado em .$format!")
                    }
                },
                onSaveProject = {
                    saveProjectLocally()
                    scope.launch {
                        snackbarHostState.showSnackbar("Projeto salvo localmente com sucesso!")
                    }
                },
                onToggleAiDrawer = { showAiDrawer = !showAiDrawer },
                onBackToHome = {
                    saveProjectLocally()
                    onBackToHome()
                }
            )
        },
        bottomBar = {
            TimelinePanel(
                scene = scene,
                onTogglePlay = {
                    scene.isPlaying = !scene.isPlaying
                    refreshUi()
                },
                onFrameChange = { frame ->
                    scene.currentFrame = frame
                    refreshUi()
                },
                onInsertKeyframe = {
                    commandExecutor.addKeyframe(scene, scene.currentFrame)
                    saveProjectLocally()
                    scope.launch { snackbarHostState.showSnackbar("Keyframe inserido no frame ${scene.currentFrame}!") }
                    refreshUi()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Center Workspace Area: Left Outliner, Middle Viewport 3D, Right Properties Panel
            Row(modifier = Modifier.fillMaxSize()) {
                // Outliner Hierarchy Panel
                OutlinerPanel(
                    scene = scene,
                    onSelectObject = { id ->
                        scene.activeObjectId = id
                        refreshUi()
                    },
                    onToggleVisibility = { id ->
                        val obj = scene.objects.find { it.id == id }
                        if (obj != null) {
                            obj.isVisible = !obj.isVisible
                            refreshUi()
                        }
                    },
                    onDuplicateObject = {
                        scene.duplicateActiveObject()
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Objeto duplicado.") }
                        refreshUi()
                    },
                    onDeleteObject = { id ->
                        scene.deleteObject(id)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Objeto removido.") }
                        refreshUi()
                    }
                )

                // Viewport 3D Canvas
                Viewport3D(
                    scene = scene,
                    selectedGizmoMode = "MOVE",
                    onSelectObject = { id ->
                        scene.activeObjectId = id
                        refreshUi()
                    },
                    modifier = Modifier.weight(1f)
                )

                // Properties Inspector Panel
                PropertiesPanel(
                    scene = scene,
                    onExtrudeFaces = { dist ->
                        commandExecutor.extrudeSelectedFaces(scene, dist)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Faces extrudadas.") }
                        refreshUi()
                    },
                    onSubdivideMesh = {
                        commandExecutor.subdivideActiveMesh(scene)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Malha subdividida.") }
                        refreshUi()
                    },
                    onAddModifier = { modType ->
                        commandExecutor.addModifier(scene, modType)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Modificador adicionado.") }
                        refreshUi()
                    },
                    onUpdateTransform = { newT ->
                        scene.getActiveObject()?.let {
                            it.transform = newT
                            saveProjectLocally()
                            refreshUi()
                        }
                    },
                    onUpdateMaterial = { color, met, rough ->
                        scene.getActiveObject()?.let {
                            it.material = it.material.copy(baseColor = color, metallic = met, roughness = rough)
                            saveProjectLocally()
                            refreshUi()
                        }
                    }
                )
            }

            // AI / MCP Control Drawer
            if (showAiDrawer) {
                AiAssistantDrawer(
                    scene = scene,
                    mcpBridge = mcpBridge,
                    npmPluginManager = npmPluginManager,
                    scriptRunner = scriptRunner,
                    onCloseDrawer = { showAiDrawer = false },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

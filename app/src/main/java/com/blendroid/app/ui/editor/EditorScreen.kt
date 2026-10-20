package com.blendroid.app.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.blendroid.app.engine.model.SceneGraph
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

    var showLeftDrawer by remember { mutableStateOf(false) }
    var showRightDrawer by remember { mutableStateOf(false) }
    var showTimeline by remember { mutableStateOf(false) }
    var showAiDrawer by remember { mutableStateOf(false) }

    var triggerRecompose by remember { mutableStateOf(0) }

    fun refreshUi() {
        triggerRecompose++
    }

    fun saveProjectLocally() {
        val json = BlendroidExporter.exportToJson(scene)
        ProjectStorageManager.saveProjectScene(context, projectId, json)
    }

    fun handleAddPrimitive(primName: String) {
        scene.pushUndoSnapshot()
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
    }

    // Animation Playback Loop
    LaunchedEffect(scene.isPlaying) {
        while (scene.isPlaying) {
            delay(1000L / 24L) // 24 FPS
            scene.currentFrame = if (scene.currentFrame >= scene.endFrame) scene.startFrame else scene.currentFrame + 1

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
                scene = scene,
                projectName = projectName,
                onSaveProject = {
                    saveProjectLocally()
                    scope.launch { snackbarHostState.showSnackbar("Projeto salvo localmente com sucesso!") }
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
                onToggleLeftDrawer = { showLeftDrawer = !showLeftDrawer },
                onToggleRightDrawer = { showRightDrawer = !showRightDrawer },
                onToggleAiDrawer = { showAiDrawer = !showAiDrawer },
                onBackToHome = {
                    saveProjectLocally()
                    onBackToHome()
                },
                onRefreshUi = { refreshUi() }
            )
        },
        bottomBar = {
            Column {
                // Collapsible Mobile Timeline Panel
                AnimatedVisibility(
                    visible = showTimeline,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
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
                            scene.pushUndoSnapshot()
                            commandExecutor.addKeyframe(scene, scene.currentFrame)
                            saveProjectLocally()
                            scope.launch { snackbarHostState.showSnackbar("Keyframe inserido no frame ${scene.currentFrame}!") }
                            refreshUi()
                        },
                        onRefreshUi = { refreshUi() }
                    )
                }

                // Fixed Mobile Bottom Toolbar
                BottomToolbar(
                    scene = scene,
                    onAddPrimitive = { prim -> handleAddPrimitive(prim) },
                    onExtrude = {
                        scene.pushUndoSnapshot()
                        commandExecutor.extrudeSelectedFaces(scene, 0.5f)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Faces extrudadas.") }
                        refreshUi()
                    },
                    onSubdivide = {
                        scene.pushUndoSnapshot()
                        commandExecutor.subdivideActiveMesh(scene)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Malha subdividida.") }
                        refreshUi()
                    },
                    onToggleTimeline = { showTimeline = !showTimeline },
                    onRefreshUi = { refreshUi() }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fullscreen 3D Viewport Canvas
            Viewport3D(
                scene = scene,
                onRefreshUi = { refreshUi() },
                modifier = Modifier.fillMaxSize()
            )

            // Left Drawer (Outliner Hierarchy)
            AnimatedVisibility(
                visible = showLeftDrawer,
                enter = slideInHorizontally(),
                exit = slideOutHorizontally(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
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
                        scene.pushUndoSnapshot()
                        scene.duplicateActiveObject()
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Objeto duplicado.") }
                        refreshUi()
                    },
                    onDeleteObject = { id ->
                        scene.pushUndoSnapshot()
                        scene.deleteObject(id)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Objeto removido.") }
                        refreshUi()
                    },
                    onCloseDrawer = { showLeftDrawer = false },
                    onRefreshUi = { refreshUi() }
                )
            }

            // Right Drawer (Properties Inspector)
            AnimatedVisibility(
                visible = showRightDrawer,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                PropertiesPanel(
                    scene = scene,
                    onExtrudeFaces = { dist ->
                        scene.pushUndoSnapshot()
                        commandExecutor.extrudeSelectedFaces(scene, dist)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Faces extrudadas.") }
                        refreshUi()
                    },
                    onSubdivideMesh = {
                        scene.pushUndoSnapshot()
                        commandExecutor.subdivideActiveMesh(scene)
                        saveProjectLocally()
                        scope.launch { snackbarHostState.showSnackbar("Malha subdividida.") }
                        refreshUi()
                    },
                    onAddModifier = { modType ->
                        scene.pushUndoSnapshot()
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
                    },
                    onCloseDrawer = { showRightDrawer = false },
                    onRefreshUi = { refreshUi() }
                )
            }

            // Bottom Drawer (AI / MCP Extension Control)
            AnimatedVisibility(
                visible = showAiDrawer,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AiAssistantDrawer(
                    scene = scene,
                    mcpBridge = mcpBridge,
                    npmPluginManager = npmPluginManager,
                    scriptRunner = scriptRunner,
                    onCloseDrawer = { showAiDrawer = false }
                )
            }
        }
    }
}

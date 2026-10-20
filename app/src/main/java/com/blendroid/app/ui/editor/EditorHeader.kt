package com.blendroid.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blendroid.app.engine.model.EditorMode
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.ui.theme.DarkSurface
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.TextMuted
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun EditorHeader(
    scene: SceneGraph,
    projectName: String,
    onSaveProject: () -> Unit,
    onExport: (String) -> Unit,
    onToggleLeftDrawer: () -> Unit,
    onToggleRightDrawer: () -> Unit,
    onToggleAiDrawer: () -> Unit,
    onBackToHome: () -> Unit,
    onRefreshUi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMainMenu by remember { mutableStateOf(false) }
    var showWorkspaceMenu by remember { mutableStateOf(false) }
    var showSceneLayerMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: Hamburger Menu & Back
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hamburger Menu Button
                Box {
                    IconButton(
                        onClick = { showMainMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Principal",
                            tint = PrimaryIndigoLight
                        )
                    }

                    DropdownMenu(
                        expanded = showMainMenu,
                        onDismissRequest = { showMainMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        Text(
                            text = "Blendroid Blender Mobile ($projectName)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigoLight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        HorizontalDivider(color = DarkSurface)

                        // File Submenu
                        DropdownMenuItem(
                            text = { Text("Salvar Projeto Local", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = PrimaryIndigoLight) },
                            onClick = { showMainMenu = false; onSaveProject() }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar .OBJ + .MTL", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = TextSecondary) },
                            onClick = { showMainMenu = false; onExport("OBJ") }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar .STL (3D)", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = TextSecondary) },
                            onClick = { showMainMenu = false; onExport("STL") }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar .GLTF 2.0", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = TextSecondary) },
                            onClick = { showMainMenu = false; onExport("GLTF") }
                        )

                        HorizontalDivider(color = DarkSurface)

                        // Render Submenu
                        DropdownMenuItem(
                            text = { Text("Renderizar Imagem", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary) },
                            onClick = { showMainMenu = false; onExport("PNG") }
                        )

                        // Help / Exit
                        DropdownMenuItem(
                            text = { Text("Voltar para Projetos", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextMuted) },
                            onClick = { showMainMenu = false; onBackToHome() }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Left Outliner Drawer Toggle
                IconButton(
                    onClick = onToggleLeftDrawer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                        contentDescription = "Outliner Drawer",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Center Group: Workspace Switcher Dropdown
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .clickable { showWorkspaceMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = scene.activeWorkspace,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = PrimaryIndigoLight
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("▼", fontSize = 9.sp, color = TextMuted)
                    }
                }

                DropdownMenu(
                    expanded = showWorkspaceMenu,
                    onDismissRequest = { showWorkspaceMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    listOf("Layout", "Modeling", "Sculpting", "Animation", "Rendering", "Geometry Nodes").forEach { ws ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = ws,
                                    color = if (ws == scene.activeWorkspace) PrimaryIndigoLight else TextPrimary,
                                    fontWeight = if (ws == scene.activeWorkspace) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                scene.activeWorkspace = ws
                                showWorkspaceMenu = false
                                // Adapt mode based on workspace
                                when (ws) {
                                    "Sculpting" -> scene.editorMode = EditorMode.SCULPT_MODE
                                    "Modeling" -> scene.editorMode = EditorMode.EDIT_MODE
                                    else -> scene.editorMode = EditorMode.OBJECT_MODE
                                }
                                onRefreshUi()
                            }
                        )
                    }
                }
            }

            // Right Group: Undo, Redo, Scene/View Layer, Properties Drawer Toggle & AI Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Undo
                IconButton(
                    onClick = {
                        if (scene.undo()) {
                            onRefreshUi()
                        }
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Desfazer (Undo)",
                        tint = if (scene.undoStack.isNotEmpty()) TextPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = {
                        if (scene.redo()) {
                            onRefreshUi()
                        }
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Refazer (Redo)",
                        tint = if (scene.redoStack.isNotEmpty()) TextPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // View Layer / Scene Popup
                Box {
                    IconButton(
                        onClick = { showSceneLayerMenu = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Camadas da Cena",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSceneLayerMenu,
                        onDismissRequest = { showSceneLayerMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        Text("Cenas & View Layers", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.padding(8.dp))
                        DropdownMenuItem(
                            text = { Text("Scene (Cena Principal)", color = PrimaryIndigoLight) },
                            onClick = { showSceneLayerMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("ViewLayer (Camada de View)", color = TextPrimary) },
                            onClick = { showSceneLayerMenu = false }
                        )
                    }
                }

                // Right Properties Drawer Toggle
                IconButton(
                    onClick = onToggleRightDrawer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Properties Drawer",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // AI Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8B5CF6))
                        .clickable { onToggleAiDrawer() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("IA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

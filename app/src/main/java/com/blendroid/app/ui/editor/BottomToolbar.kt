package com.blendroid.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blendroid.app.engine.model.EditorMode
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.ui.theme.DarkSurface
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigo
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.TextMuted
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun BottomToolbar(
    scene: SceneGraph,
    onAddPrimitive: (String) -> Unit,
    onExtrude: () -> Unit,
    onSubdivide: () -> Unit,
    onToggleTimeline: () -> Unit,
    onRefreshUi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSelectMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Select Tool
            Box {
                ToolButton(
                    icon = Icons.Default.TouchApp,
                    label = "Select (${scene.selectMode})",
                    isSelected = scene.activeTool == "SELECT",
                    onClick = {
                        if (scene.activeTool == "SELECT") {
                            showSelectMenu = true
                        } else {
                            scene.activeTool = "SELECT"
                            onRefreshUi()
                        }
                    }
                )

                DropdownMenu(
                    expanded = showSelectMenu,
                    onDismissRequest = { showSelectMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    listOf("BOX", "CIRCLE", "LASSO").forEach { mode ->
                        DropdownMenuItem(
                            text = { Text("Seleção $mode", color = if (mode == scene.selectMode) PrimaryIndigoLight else TextPrimary) },
                            onClick = {
                                scene.selectMode = mode
                                scene.activeTool = "SELECT"
                                showSelectMenu = false
                                onRefreshUi()
                            }
                        )
                    }
                }
            }

            // Move Tool (G)
            ToolButton(
                icon = Icons.Default.OpenWith,
                label = "Move (G)",
                isSelected = scene.activeTool == "MOVE",
                onClick = {
                    scene.activeTool = "MOVE"
                    onRefreshUi()
                }
            )

            // Rotate Tool (R)
            ToolButton(
                icon = Icons.Default.Refresh,
                label = "Rotate (R)",
                isSelected = scene.activeTool == "ROTATE",
                onClick = {
                    scene.activeTool = "ROTATE"
                    onRefreshUi()
                }
            )

            // Scale Tool (S)
            ToolButton(
                icon = Icons.Default.ZoomOutMap,
                label = "Scale (S)",
                isSelected = scene.activeTool == "SCALE",
                onClick = {
                    scene.activeTool = "SCALE"
                    onRefreshUi()
                }
            )

            // Contextual Tool based on Mode
            when (scene.editorMode) {
                EditorMode.EDIT_MODE -> {
                    ToolButton(
                        icon = Icons.Default.Splitscreen,
                        label = "Extrude",
                        isSelected = scene.activeTool == "EXTRUDE",
                        onClick = {
                            scene.activeTool = "EXTRUDE"
                            onExtrude()
                            onRefreshUi()
                        }
                    )
                }
                EditorMode.SCULPT_MODE -> {
                    ToolButton(
                        icon = Icons.Default.Brush,
                        label = "Sculpt",
                        isSelected = scene.activeTool == "SCULPT_BRUSH",
                        onClick = {
                            scene.activeTool = "SCULPT_BRUSH"
                            onRefreshUi()
                        }
                    )
                }
                else -> {
                    // Annotate Tool
                    ToolButton(
                        icon = Icons.Default.Edit,
                        label = "Annotate",
                        isSelected = scene.activeTool == "ANNOTATE",
                        onClick = {
                            scene.activeTool = "ANNOTATE"
                            onRefreshUi()
                        }
                    )
                }
            }

            // Measure Tool
            ToolButton(
                icon = Icons.Default.Straighten,
                label = "Measure",
                isSelected = scene.activeTool == "MEASURE",
                onClick = {
                    scene.activeTool = "MEASURE"
                    onRefreshUi()
                }
            )

            // Add Object (+)
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo)
                        .clickable { showAddMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    Text("Adicionar Primitiva 3D", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.padding(8.dp))
                    listOf("Cubo", "Esfera", "Cilindro", "Plano", "Torus", "Suzanne (Macaco)", "Esqueleto (Armature)", "Luz Ponto", "Câmera").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item, color = TextPrimary) },
                            onClick = {
                                showAddMenu = false
                                onAddPrimitive(item)
                            }
                        )
                    }
                }
            }

            // Secondary Overflow Menu "..."
            Box {
                IconButton(
                    onClick = { showMoreMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Mais Ferramentas",
                        tint = TextSecondary
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Subdividir Malha (Subdivide)", color = TextPrimary) },
                        onClick = {
                            showMoreMenu = false
                            onSubdivide()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Extrudar Faces (Extrude E)", color = TextPrimary) },
                        onClick = {
                            showMoreMenu = false
                            onExtrude()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicar Ativo (Shift+D)", color = TextPrimary) },
                        onClick = {
                            showMoreMenu = false
                            scene.duplicateActiveObject()
                            onRefreshUi()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir Ativo (X)", color = Color(0xFFEF4444)) },
                        onClick = {
                            showMoreMenu = false
                            scene.activeObjectId?.let { scene.deleteObject(it) }
                            onRefreshUi()
                        }
                    )
                }
            }

            // Timeline Toggle Button
            IconButton(
                onClick = onToggleTimeline,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Timeline",
                    tint = PrimaryIndigoLight
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PrimaryIndigo else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

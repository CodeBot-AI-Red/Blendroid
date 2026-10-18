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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.blendroid.app.engine.model.ShadingMode
import com.blendroid.app.ui.theme.DarkBackground
import com.blendroid.app.ui.theme.DarkSurface
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigo
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun EditorHeader(
    projectName: String,
    editorMode: EditorMode,
    shadingMode: ShadingMode,
    onModeChange: (EditorMode) -> Unit,
    onShadingChange: (ShadingMode) -> Unit,
    onAddPrimitive: (String) -> Unit,
    onExport: (String) -> Unit,
    onSaveProject: () -> Unit,
    onToggleAiDrawer: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFileMenu by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showModeMenu by remember { mutableStateOf(false) }
    var showShadingMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: Back, Title, File Menu, Add Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = projectName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryIndigoLight,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Menu Arquivo
                Box {
                    HeaderMenuButton(text = "Arquivo") { showFileMenu = true }
                    DropdownMenu(
                        expanded = showFileMenu,
                        onDismissRequest = { showFileMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Salvar Projeto (.blendroid)", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = PrimaryIndigoLight) },
                            onClick = {
                                showFileMenu = false
                                onSaveProject()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar .OBJ + .MTL", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = TextSecondary) },
                            onClick = {
                                showFileMenu = false
                                onExport("OBJ")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar .STL (Impressão 3D)", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = TextSecondary) },
                            onClick = {
                                showFileMenu = false
                                onExport("STL")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar .GLTF 2.0", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = TextSecondary) },
                            onClick = {
                                showFileMenu = false
                                onExport("GLTF")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Menu Adicionar
                Box {
                    HeaderMenuButton(text = "Adicionar +") { showAddMenu = true }
                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        listOf("Cubo", "Esfera", "Cilindro", "Plano", "Torus", "Suzanne (Macaco)", "Esqueleto (Armature)", "Luz Ponto", "Câmera").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryIndigoLight) },
                                onClick = {
                                    showAddMenu = false
                                    onAddPrimitive(item)
                                }
                            )
                        }
                    }
                }
            }

            // Middle Group: Mode Selector & Shading Selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Editor Mode Selector
                Box {
                    val modeLabel = when (editorMode) {
                        EditorMode.OBJECT_MODE -> "Modo Objeto"
                        EditorMode.EDIT_MODE -> "Modo Edição"
                        EditorMode.SCULPT_MODE -> "Modo Escultura"
                        EditorMode.POSE_MODE -> "Modo Pose"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryIndigo)
                            .clickable { showModeMenu = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = modeLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showModeMenu,
                        onDismissRequest = { showModeMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        EditorMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (mode) {
                                            EditorMode.OBJECT_MODE -> "Modo Objeto"
                                            EditorMode.EDIT_MODE -> "Modo Edição"
                                            EditorMode.SCULPT_MODE -> "Modo Escultura"
                                            EditorMode.POSE_MODE -> "Modo Pose (Bones)"
                                        },
                                        color = if (mode == editorMode) PrimaryIndigoLight else TextPrimary
                                    )
                                },
                                onClick = {
                                    showModeMenu = false
                                    onModeChange(mode)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Shading Mode Selector
                Box {
                    val shadeLabel = when (shadingMode) {
                        ShadingMode.WIREFRAME -> "Wireframe"
                        ShadingMode.SOLID -> "Sólido"
                        ShadingMode.MATERIAL -> "Material"
                        ShadingMode.RENDERED -> "Renderizado"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                            .clickable { showShadingMenu = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = shadeLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showShadingMenu,
                        onDismissRequest = { showShadingMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        ShadingMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name, color = TextPrimary) },
                                onClick = {
                                    showShadingMenu = false
                                    onShadingChange(mode)
                                }
                            )
                        }
                    }
                }
            }

            // Right Group: AI / MCP Control Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onToggleAiDrawer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA MCP",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IA / MCP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HeaderMenuButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = TextSecondary
        )
    }
}

package com.blendroid.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.blendroid.app.engine.model.Color4
import com.blendroid.app.engine.model.ModifierType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.Transform
import com.blendroid.app.engine.model.Vector3
import com.blendroid.app.ui.theme.DarkCardBorder
import com.blendroid.app.ui.theme.DarkSurface
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigo
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.TextMuted
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun PropertiesPanel(
    scene: SceneGraph,
    onExtrudeFaces: (Float) -> Unit,
    onSubdivideMesh: () -> Unit,
    onAddModifier: (ModifierType) -> Unit,
    onUpdateTransform: (Transform) -> Unit,
    onUpdateMaterial: (Color4, Float, Float) -> Unit,
    onCloseDrawer: () -> Unit,
    onRefreshUi: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Blender Properties Tabs
    var selectedTab by remember { mutableStateOf("OBJECT") }
    val activeObj = scene.getActiveObject()

    val tabs = listOf(
        Triple("TOOL", Icons.Default.Tune, "Active Tool"),
        Triple("RENDER", Icons.Default.CameraAlt, "Render"),
        Triple("SCENE", Icons.Default.Public, "Scene"),
        Triple("OBJECT", Icons.Default.OpenWith, "Object"),
        Triple("MODIFIERS", Icons.Default.Build, "Modifiers"),
        Triple("DATA", Icons.Default.ViewInAr, "Data"),
        Triple("MATERIAL", Icons.Default.ColorLens, "Material"),
        Triple("CONSTRAINTS", Icons.Default.Link, "Constraints")
    )

    Surface(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(6.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Properties",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = TextPrimary
                )

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar Drawer",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Horizontal Scrollable Icon Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEach { (tabKey, icon, label) ->
                    val isSel = selectedTab == tabKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) PrimaryIndigo else DarkSurfaceVariant)
                            .clickable { selectedTab = tabKey }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSel) Color.White else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(6.dp))

            // Tab Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    "TOOL" -> {
                        CollapsibleCard(title = "Active Tool Settings") {
                            Text("Ferramenta Ativa: ${scene.activeTool}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigoLight)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Modo de Seleção: ${scene.selectMode}", fontSize = 11.sp, color = TextSecondary)
                            Text("Pincel de Escultura: ${scene.sculptBrush}", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    "RENDER" -> {
                        CollapsibleCard(title = "Engine de Render") {
                            Text("Engine: EEVEE Mobile (GL ES 2.0)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Amostras de Viewport: 16", fontSize = 10.sp, color = TextSecondary)
                            Text("Amostras de Render: 64", fontSize = 10.sp, color = TextSecondary)
                            Text("Ambient Occlusion: Ativado", fontSize = 10.sp, color = TextSecondary)
                            Text("Bloom: Ativado", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    "SCENE" -> {
                        CollapsibleCard(title = "Propriedades da Cena") {
                            Text("Unidades: Métrico (Metros)", fontSize = 11.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gravidade: Z = -9.81 m/s²", fontSize = 11.sp, color = TextSecondary)
                            Text("FPS: 24.0", fontSize = 11.sp, color = TextSecondary)
                            Text("Frame Inicial: ${scene.startFrame}", fontSize = 11.sp, color = TextSecondary)
                            Text("Frame Final: ${scene.endFrame}", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    "OBJECT" -> {
                        if (activeObj == null) {
                            Text("Nenhum objeto selecionado", fontSize = 11.sp, color = TextMuted)
                        } else {
                            Text("Objeto: ${activeObj.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))

                            CollapsibleCard(title = "Transform (Posição, Rotação, Escala)") {
                                val t = activeObj.transform

                                Text("Posição (X, Y, Z)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Vector3TouchControl(
                                    v = t.position,
                                    onValueChange = { newPos ->
                                        scene.pushUndoSnapshot()
                                        onUpdateTransform(t.copy(position = newPos))
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Rotação (°)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Vector3TouchControl(
                                    v = t.rotation,
                                    onValueChange = { newRot ->
                                        scene.pushUndoSnapshot()
                                        onUpdateTransform(t.copy(rotation = newRot))
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Escala", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Vector3TouchControl(
                                    v = t.scale,
                                    onValueChange = { newScl ->
                                        scene.pushUndoSnapshot()
                                        onUpdateTransform(t.copy(scale = newScl))
                                    }
                                )
                            }
                        }
                    }

                    "MODIFIERS" -> {
                        if (activeObj == null) {
                            Text("Nenhum objeto selecionado", fontSize = 11.sp, color = TextMuted)
                        } else {
                            CollapsibleCard(title = "Modificadores (${activeObj.modifiers.size})") {
                                activeObj.modifiers.forEach { mod ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkSurfaceVariant)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(mod.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Button(
                                            onClick = {
                                                activeObj.modifiers.removeIf { it.id == mod.id }
                                                scene.pushUndoSnapshot()
                                                onRefreshUi()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Remover", fontSize = 9.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Adicionar Modificador:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

                                listOf(
                                    Pair("Subdivision Surface", ModifierType.SUBDIVISION_SURFACE),
                                    Pair("Mirror (Espelhar)", ModifierType.MIRROR),
                                    Pair("Array (Matriz)", ModifierType.ARRAY),
                                    Pair("Solidify (Solidificar)", ModifierType.SOLIDIFY)
                                ).forEach { (label, type) ->
                                    Button(
                                        onClick = {
                                            onAddModifier(type)
                                            scene.pushUndoSnapshot()
                                            onRefreshUi()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo.copy(alpha = 0.25f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Text("+ $label", fontSize = 10.sp, color = PrimaryIndigoLight)
                                    }
                                }
                            }
                        }
                    }

                    "DATA" -> {
                        if (activeObj == null) {
                            Text("Nenhum objeto selecionado", fontSize = 11.sp, color = TextMuted)
                        } else {
                            CollapsibleCard(title = "Geometria da Malha") {
                                val mesh = activeObj.mesh
                                Text("Vértices: ${mesh.vertices.size}", fontSize = 11.sp, color = TextSecondary)
                                Text("Faces: ${mesh.faces.size}", fontSize = 11.sp, color = TextSecondary)

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onExtrudeFaces(0.5f); scene.pushUndoSnapshot(); onRefreshUi() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Extrudar Faces (E)", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = { onSubdivideMesh(); scene.pushUndoSnapshot(); onRefreshUi() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Subdividir Malha (Subdivide)", fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    "MATERIAL" -> {
                        if (activeObj == null) {
                            Text("Nenhum objeto selecionado", fontSize = 11.sp, color = TextMuted)
                        } else {
                            CollapsibleCard(title = "Propriedades do Material") {
                                val mat = activeObj.material
                                var r by remember(mat.baseColor.r) { mutableStateOf(mat.baseColor.r) }
                                var g by remember(mat.baseColor.g) { mutableStateOf(mat.baseColor.g) }
                                var b by remember(mat.baseColor.b) { mutableStateOf(mat.baseColor.b) }
                                var metallic by remember(mat.metallic) { mutableStateOf(mat.metallic) }
                                var roughness by remember(mat.roughness) { mutableStateOf(mat.roughness) }

                                Text("Material: ${mat.name}", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Color Preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(mat.baseColor.toArgbInt()))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Vermelho (R): ${(r * 255).toInt()}", fontSize = 10.sp, color = TextMuted)
                                Slider(
                                    value = r,
                                    onValueChange = { r = it; onUpdateMaterial(Color4(r, g, b, 1f), metallic, roughness) },
                                    colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                                )

                                Text("Verde (G): ${(g * 255).toInt()}", fontSize = 10.sp, color = TextMuted)
                                Slider(
                                    value = g,
                                    onValueChange = { g = it; onUpdateMaterial(Color4(r, g, b, 1f), metallic, roughness) },
                                    colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                                )

                                Text("Azul (B): ${(b * 255).toInt()}", fontSize = 10.sp, color = TextMuted)
                                Slider(
                                    value = b,
                                    onValueChange = { b = it; onUpdateMaterial(Color4(r, g, b, 1f), metallic, roughness) },
                                    colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                                )

                                Text("Metálico: ${(metallic * 100).toInt()}%", fontSize = 10.sp, color = TextMuted)
                                Slider(
                                    value = metallic,
                                    onValueChange = { metallic = it; onUpdateMaterial(Color4(r, g, b, 1f), metallic, roughness) },
                                    colors = SliderDefaults.colors(thumbColor = PrimaryIndigoLight, activeTrackColor = PrimaryIndigo)
                                )

                                Text("Rugosidade: ${(roughness * 100).toInt()}%", fontSize = 10.sp, color = TextMuted)
                                Slider(
                                    value = roughness,
                                    onValueChange = { roughness = it; onUpdateMaterial(Color4(r, g, b, 1f), metallic, roughness) },
                                    colors = SliderDefaults.colors(thumbColor = PrimaryIndigoLight, activeTrackColor = PrimaryIndigo)
                                )
                            }
                        }
                    }

                    "CONSTRAINTS" -> {
                        CollapsibleCard(title = "Constraints (Restrições)") {
                            Text("Restrições ativas: 0", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsibleCard(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(if (expanded) "▲" else "▼", fontSize = 10.sp, color = TextMuted)
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun Vector3TouchControl(
    v: Vector3,
    onValueChange: (Vector3) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            Triple("X", v.x, Color(0xFFEF4444)),
            Triple("Y", v.y, Color(0xFF10B981)),
            Triple("Z", v.z, Color(0xFF3B82F6))
        ).forEach { (axis, valNum, color) ->
            Column(modifier = Modifier.weight(1f)) {
                Text(axis, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = String.format("%.2f", valNum),
                    onValueChange = { text ->
                        val parsed = text.toFloatOrNull() ?: valNum
                        val newV = when (axis) {
                            "X" -> v.copy(x = parsed)
                            "Y" -> v.copy(y = parsed)
                            else -> v.copy(z = parsed)
                        }
                        onValueChange(newV)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = color,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        }
    }
}

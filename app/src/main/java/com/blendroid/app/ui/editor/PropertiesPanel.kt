package com.blendroid.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("TRANSFORM") } // TRANSFORM, MATERIAL, MESH, MODIFIERS, ARMATURE
    val activeObj = scene.getActiveObject()

    Surface(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(6.dp)
        ) {
            // Properties Panel Tab Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf(
                    Pair("TRANSFORM", Icons.Default.OpenWith),
                    Pair("MATERIAL", Icons.Default.ColorLens),
                    Pair("MESH", Icons.Default.Build),
                    Pair("MODIFIERS", Icons.Default.Tune),
                    Pair("ARMATURE", Icons.Default.Person)
                ).forEach { (tabKey, icon) ->
                    val isSel = selectedTab == tabKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) PrimaryIndigo else DarkSurfaceVariant)
                            .clickable { selectedTab = tabKey }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tabKey,
                            tint = if (isSel) Color.White else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(6.dp))

            if (activeObj == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum objeto selecionado", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Objeto: ${activeObj.name}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when (selectedTab) {
                        "TRANSFORM" -> {
                            val t = activeObj.transform

                            Text("Posição (X, Y, Z)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Vector3Control(
                                v = t.position,
                                onValueChange = { newPos -> onUpdateTransform(t.copy(position = newPos)) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Rotação (°)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Vector3Control(
                                v = t.rotation,
                                onValueChange = { newRot -> onUpdateTransform(t.copy(rotation = newRot)) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Escala", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Vector3Control(
                                v = t.scale,
                                onValueChange = { newScl -> onUpdateTransform(t.copy(scale = newScl)) }
                            )
                        }

                        "MATERIAL" -> {
                            val mat = activeObj.material
                            var r by remember(mat.baseColor.r) { mutableStateOf(mat.baseColor.r) }
                            var g by remember(mat.baseColor.g) { mutableStateOf(mat.baseColor.g) }
                            var b by remember(mat.baseColor.b) { mutableStateOf(mat.baseColor.b) }
                            var metallic by remember(mat.metallic) { mutableStateOf(mat.metallic) }
                            var roughness by remember(mat.roughness) { mutableStateOf(mat.roughness) }

                            Text("Material: ${mat.name}", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Color Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
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

                        "MESH" -> {
                            val mesh = activeObj.mesh
                            Text("Vértices: ${mesh.vertices.size}", fontSize = 11.sp, color = TextSecondary)
                            Text("Faces: ${mesh.faces.size}", fontSize = 11.sp, color = TextSecondary)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Operações de Edição de Malha", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = { onExtrudeFaces(0.5f) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Extrudar Faces (Extrude E)", fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = onSubdivideMesh,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Subdividir Malha (Subdivide)", fontSize = 11.sp, color = TextPrimary)
                            }
                        }

                        "MODIFIERS" -> {
                            Text("Modificadores Ativos (${activeObj.modifiers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                            activeObj.modifiers.forEach { mod ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(8.dp)
                                ) {
                                    Text(mod.name, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Adicionar Modificador", fontSize = 11.sp, color = TextSecondary)

                            listOf(
                                Pair("Subdivision Surface", ModifierType.SUBDIVISION_SURFACE),
                                Pair("Mirror (Espelhar)", ModifierType.MIRROR),
                                Pair("Array (Matriz)", ModifierType.ARRAY),
                                Pair("Solidify (Solidificar)", ModifierType.SOLIDIFY)
                            ).forEach { (label, type) ->
                                Button(
                                    onClick = { onAddModifier(type) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text("+ $label", fontSize = 10.sp, color = PrimaryIndigoLight)
                                }
                            }
                        }

                        "ARMATURE" -> {
                            val rig = activeObj.armature
                            if (rig == null) {
                                Text("Objeto não possui Armature/Esqueleto.", fontSize = 11.sp, color = TextMuted)
                            } else {
                                Text("Bones do Rig (${rig.bones.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                rig.bones.forEach { bone ->
                                    Text("• ${bone.name} (Parent: ${bone.parentId ?: "None"})", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Vector3Control(
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
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
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

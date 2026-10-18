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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blendroid.app.engine.model.MeshPrimitiveType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.ui.theme.DarkSurface
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigo
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.TextMuted
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun OutlinerPanel(
    scene: SceneGraph,
    onSelectObject: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onDuplicateObject: () -> Unit,
    onDeleteObject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight(),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(6.dp)
        ) {
            // Outliner Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Outliner",
                        tint = PrimaryIndigoLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Outliner (Cena)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = TextPrimary
                    )
                }

                Row {
                    IconButton(
                        onClick = onDuplicateObject,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicar",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(4.dp))

            // Object Tree List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(scene.objects, key = { it.id }) { obj ->
                    val isSelected = obj.id == scene.activeObjectId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) PrimaryIndigo.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable { onSelectObject(obj.id) }
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val icon = when (obj.type) {
                                MeshPrimitiveType.CAMERA -> Icons.Default.CameraAlt
                                MeshPrimitiveType.LIGHT -> Icons.Default.Lightbulb
                                MeshPrimitiveType.ARMATURE -> Icons.Default.Person
                                else -> Icons.Default.ViewInAr
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryIndigoLight else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = obj.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) TextPrimary else TextSecondary,
                                maxLines = 1
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Visibility toggle
                            IconButton(
                                onClick = { onToggleVisibility(obj.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (obj.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibilidade",
                                    tint = if (obj.isVisible) TextSecondary else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            // Delete
                            IconButton(
                                onClick = { onDeleteObject(obj.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

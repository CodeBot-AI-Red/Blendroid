package com.blendroid.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blendroid.app.engine.model.MeshPrimitiveType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.SceneObject
import com.blendroid.app.ui.theme.DarkCardBorder
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
    onCloseDrawer: () -> Unit,
    onRefreshUi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var contextMenuObjId by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight(),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            // Outliner Drawer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Outliner",
                        tint = PrimaryIndigoLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Outliner",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = TextPrimary
                    )
                }

                Row {
                    IconButton(
                        onClick = onDuplicateObject,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicar",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Drawer",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar objeto...", fontSize = 11.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scene Collection Tree Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = PrimaryIndigoLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scene Collection", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filtered Objects List
            val filteredObjects = scene.objects.filter {
                searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredObjects, key = { it.id }) { obj ->
                    val isSelected = obj.id == scene.activeObjectId

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryIndigo.copy(alpha = 0.35f) else Color.Transparent)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onSelectObject(obj.id) },
                                        onLongPress = { contextMenuObjId = obj.id }
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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
                                // Visibility Eye
                                IconButton(
                                    onClick = { onToggleVisibility(obj.id) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = if (obj.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Visibilidade",
                                        tint = if (obj.isVisible) TextSecondary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Selectable Pointer/Lock
                                IconButton(
                                    onClick = { obj.isSelectable = !obj.isSelectable; onRefreshUi() },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = if (obj.isSelectable) Icons.Default.TouchApp else Icons.Default.Lock,
                                        contentDescription = "Selecionável",
                                        tint = if (obj.isSelectable) TextSecondary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Render Camera
                                IconButton(
                                    onClick = { obj.isRenderable = !obj.isRenderable; onRefreshUi() },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Renderizável",
                                        tint = if (obj.isRenderable) PrimaryIndigoLight else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Long Press Context Menu
                        DropdownMenu(
                            expanded = contextMenuObjId == obj.id,
                            onDismissRequest = { contextMenuObjId = null },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            Text("Opções: ${obj.name}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.padding(8.dp))
                            DropdownMenuItem(
                                text = { Text("Duplicar Objeto", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PrimaryIndigoLight) },
                                onClick = {
                                    contextMenuObjId = null
                                    onSelectObject(obj.id)
                                    scene.duplicateActiveObject()
                                    onRefreshUi()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Excluir Objeto", color = Color(0xFFEF4444)) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                                onClick = {
                                    contextMenuObjId = null
                                    onDeleteObject(obj.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

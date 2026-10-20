package com.blendroid.app.ui.editor

import android.opengl.GLSurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.viewinterop.AndroidView
import com.blendroid.app.engine.model.EditorMode
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.ShadingMode
import com.blendroid.app.engine.model.Vector3
import com.blendroid.app.engine.model.getLabel
import com.blendroid.app.ui.theme.DarkBackground
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigo
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.StatusGreen
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun Viewport3D(
    scene: SceneGraph,
    selectedGizmoMode: String = "SELECT",
    onSelectObject: (String) -> Unit = {},
    onRefreshUi: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val renderer = remember { BlendroidGLRenderer() }
    var glView by remember { mutableStateOf<GLSurfaceView?>(null) }

    var yaw by remember { mutableStateOf(35f) }
    var pitch by remember { mutableStateOf(25f) }

    var showModeDropdown by remember { mutableStateOf(false) }
    var showOrientationDropdown by remember { mutableStateOf(false) }
    var showPivotDropdown by remember { mutableStateOf(false) }
    var showShadingDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        // Position 3D cursor at long press point
                        val normX = (offset.x / size.width - 0.5f) * 8f
                        val normY = -(offset.y / size.height - 0.5f) * 8f
                        scene.cursorPosition = Vector3(normX, normY, 0f)
                        scene.pushUndoSnapshot()
                        onRefreshUi()
                        glView?.requestRender()
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomFactor, _ ->
                    if (pan.x != 0f || pan.y != 0f) {
                        renderer.yaw += pan.x * 0.4f
                        renderer.pitch = (renderer.pitch - pan.y * 0.4f).coerceIn(-85f, 85f)
                        yaw = renderer.yaw
                        pitch = renderer.pitch
                    }
                    if (zoomFactor != 1.0f) {
                        renderer.zoom = (renderer.zoom / zoomFactor).coerceIn(2f, 150f)
                    }
                    glView?.requestRender()
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                GLSurfaceView(ctx).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(renderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    glView = this
                }
            },
            update = {
                renderer.scene = scene
            },
            modifier = Modifier.fillMaxSize()
        )

        // 1. Floating Viewport Semi-Transparent Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mode Selector
                Box {
                    val modeLabel = scene.editorMode.getLabel()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryIndigo)
                            .clickable { showModeDropdown = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(modeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    DropdownMenu(
                        expanded = showModeDropdown,
                        onDismissRequest = { showModeDropdown = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        EditorMode.values().forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.getLabel(), color = if (m == scene.editorMode) PrimaryIndigoLight else TextPrimary) },
                                onClick = {
                                    scene.editorMode = m
                                    showModeDropdown = false
                                    onRefreshUi()
                                }
                            )
                        }
                    }
                }

                // Orientation Selector
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { showOrientationDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(scene.transformOrientation, fontSize = 10.sp, color = TextPrimary)
                    }

                    DropdownMenu(
                        expanded = showOrientationDropdown,
                        onDismissRequest = { showOrientationDropdown = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        listOf("GLOBAL", "LOCAL", "NORMAL", "GIMBAL", "VIEW", "CURSOR").forEach { ori ->
                            DropdownMenuItem(
                                text = { Text(ori, color = if (ori == scene.transformOrientation) PrimaryIndigoLight else TextPrimary) },
                                onClick = {
                                    scene.transformOrientation = ori
                                    showOrientationDropdown = false
                                    onRefreshUi()
                                }
                            )
                        }
                    }
                }

                // Pivot Point Selector
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { showPivotDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Pivot: ${scene.pivotPoint.take(6)}", fontSize = 10.sp, color = TextPrimary)
                    }

                    DropdownMenu(
                        expanded = showPivotDropdown,
                        onDismissRequest = { showPivotDropdown = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        listOf("BOUNDING_BOX", "3D_CURSOR", "INDIVIDUAL_ORIGINS", "MEDIAN_POINT", "ACTIVE_ELEMENT").forEach { piv ->
                            DropdownMenuItem(
                                text = { Text(piv, color = if (piv == scene.pivotPoint) PrimaryIndigoLight else TextPrimary) },
                                onClick = {
                                    scene.pivotPoint = piv
                                    showPivotDropdown = false
                                    onRefreshUi()
                                }
                            )
                        }
                    }
                }

                // Snap Toggle & Selector
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (scene.snapEnabled) PrimaryIndigo else DarkSurfaceVariant)
                            .clickable { scene.snapEnabled = !scene.snapEnabled; onRefreshUi() }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Adjust, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(scene.snapType, fontSize = 10.sp, color = Color.White)
                    }
                }

                // Proportional Editing Toggle
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (scene.proportionalEditing) PrimaryIndigo else DarkSurfaceVariant)
                            .clickable { scene.proportionalEditing = !scene.proportionalEditing; onRefreshUi() }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prop: ${scene.proportionalFalloff}", fontSize = 10.sp, color = Color.White)
                    }
                }

                // Shading Mode Selector
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryIndigoLight.copy(alpha = 0.2f))
                            .clickable { showShadingDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(scene.shadingMode.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigoLight)
                    }

                    DropdownMenu(
                        expanded = showShadingDropdown,
                        onDismissRequest = { showShadingDropdown = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        ShadingMode.values().forEach { sm ->
                            DropdownMenuItem(
                                text = { Text(sm.name, color = if (sm == scene.shadingMode) PrimaryIndigoLight else TextPrimary) },
                                onClick = {
                                    scene.shadingMode = sm
                                    showShadingDropdown = false
                                    onRefreshUi()
                                }
                            )
                        }
                    }
                }

                // Overlays Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (scene.showOverlays) PrimaryIndigo else DarkSurfaceVariant)
                        .clickable { scene.showOverlays = !scene.showOverlays; onRefreshUi() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (scene.showOverlays) Icons.Default.GridOn else Icons.Default.VisibilityOff,
                        contentDescription = "Overlays",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // X-Ray Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (scene.showXRay) PrimaryIndigo else DarkSurfaceVariant)
                        .clickable { scene.showXRay = !scene.showXRay; onRefreshUi() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("X-Ray", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // 2. Navigation Gizmo (Top Right Floating)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 58.dp, end = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Navegação 3D", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)

            // Orbit Axis Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // X (Red)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                        .clickable { renderer.setViewRight(); yaw = renderer.yaw; pitch = renderer.pitch; glView?.requestRender() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("X", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Y (Green)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xFF10B981), CircleShape)
                        .clickable { renderer.setViewFront(); yaw = renderer.yaw; pitch = renderer.pitch; glView?.requestRender() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Y", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Z (Blue)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xFF3B82F6), CircleShape)
                        .clickable { renderer.setViewTop(); yaw = renderer.yaw; pitch = renderer.pitch; glView?.requestRender() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Z", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Quick View Preset Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Camera View
                IconButton(
                    onClick = { renderer.setViewCamera(); yaw = renderer.yaw; pitch = renderer.pitch; glView?.requestRender() },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = "Câmera", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                // Isometric
                IconButton(
                    onClick = { renderer.setViewIsometric(); yaw = renderer.yaw; pitch = renderer.pitch; glView?.requestRender() },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Isométrica", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                // Reset Cursor Position
                IconButton(
                    onClick = { scene.cursorPosition = Vector3.Zero; onRefreshUi(); glView?.requestRender() },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Cursor 3D", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // 3. Viewport Overlay Active Object Info (Bottom Left Floating)
        val activeName = scene.getActiveObject()?.name ?: "Nenhum Selecionado"
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 12.dp, start = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(StatusGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$activeName | Yaw: ${yaw.toInt()}° Pitch: ${pitch.toInt()}°",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White
                )
            }
        }
    }
}

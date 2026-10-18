package com.blendroid.app.ui.editor

import android.opengl.GLSurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.ui.theme.DarkBackground
import com.blendroid.app.ui.theme.StatusGreen

@Composable
fun Viewport3D(
    scene: SceneGraph,
    selectedGizmoMode: String, // "MOVE", "ROTATE", "SCALE"
    onSelectObject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val renderer = remember { BlendroidGLRenderer() }
    var glView by remember { mutableStateOf<GLSurfaceView?>(null) }

    var yaw by remember { mutableStateOf(35f) }
    var pitch by remember { mutableStateOf(25f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomFactor, _ ->
                    renderer.yaw += pan.x * 0.4f
                    renderer.pitch = (renderer.pitch - pan.y * 0.4f).coerceIn(-85f, 85f)
                    renderer.zoom = (renderer.zoom / zoomFactor).coerceIn(2f, 150f)

                    yaw = renderer.yaw
                    pitch = renderer.pitch

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

        // Viewport Overlay Info Badge (Top Left)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            val activeName = scene.getActiveObject()?.name ?: "Nenhum Selecionado"
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(StatusGreen, shape = RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OpenGL ES 2.0 (Hardware Acelerado)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = StatusGreen
                    )
                }
                Text(
                    text = "3D Viewport | $activeName | Yaw: ${yaw.toInt()}° Pitch: ${pitch.toInt()}°",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.White
                )
            }
        }
    }
}

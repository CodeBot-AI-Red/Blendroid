package com.blendroid.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.ui.theme.DarkSurface
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigo
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.TextMuted
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun TimelinePanel(
    scene: SceneGraph,
    onTogglePlay: () -> Unit,
    onFrameChange: (Int) -> Unit,
    onInsertKeyframe: () -> Unit,
    onRefreshUi: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Playback Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Jump Start
                    IconButton(
                        onClick = { onFrameChange(scene.startFrame) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Primeiro Frame", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }

                    // Play / Pause
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryIndigo, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (scene.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Tocar / Pausar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Jump End
                    IconButton(
                        onClick = { onFrameChange(scene.endFrame) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Último Frame", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Insert Keyframe Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444))
                            .clickable { onInsertKeyframe() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Keyframe", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Auto-Keying Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (scene.isAutoKeying) Color(0xFF3B82F6) else DarkSurfaceVariant)
                            .clickable { scene.isAutoKeying = !scene.isAutoKeying; onRefreshUi() }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.RadioButtonChecked, contentDescription = "Auto Key", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                // Timeline Frame Scrubber
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${scene.currentFrame}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryIndigoLight,
                        modifier = Modifier.width(28.dp)
                    )

                    Slider(
                        value = scene.currentFrame.toFloat(),
                        onValueChange = { onFrameChange(it.toInt()) },
                        valueRange = scene.startFrame.toFloat()..scene.endFrame.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryIndigoLight,
                            activeTrackColor = PrimaryIndigo,
                            inactiveTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${scene.endFrame}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.width(28.dp)
                    )
                }

                // Keyframe Counter Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Keys: ${scene.animationTrack.keyframes.size}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

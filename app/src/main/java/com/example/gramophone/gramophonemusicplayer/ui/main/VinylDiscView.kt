package com.example.gramophonemusicplayer.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2

@Composable
fun VinylDiscView(
    isPlaying: Boolean,
    songTitle: String,
    artistName: String,
    onPlayPauseClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var currentRotation by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size(300.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val touchPoint = change.position
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Calculate angle between center and touch point
                    val angle = Math.toDegrees(
                        atan2(
                            (touchPoint.y - center.y).toDouble(),
                            (touchPoint.x - center.x).toDouble()
                        ).toDouble()
                    ).toFloat()

                    // Simple rotational detection: check direction of change
                    // This is a simplified version; for production, track the delta of the angle
                    if (dragAmount.x > 0) {
                        onSeekForward()
                    } else if (dragAmount.x < 0) {
                        onSeekBackward()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            // Only rotate if playing
            val rotationAngle = if (isPlaying) rotation else 0f

            rotate(rotationAngle) {
                // Main Black Disc
                drawCircle(
                    color = Color(0xFF1A1A1A),
                    radius = radius,
                    center = center
                )

                // Vinyl Grooves (Concentric circles)
                for (i in 10..150 step 10) {
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        radius = radius - i,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }

                // Label Circle (Center)
                drawCircle(
                    color = Color(0xFFC0C0C0), // Silver/Light gray center
                    radius = radius * 0.3f,
                    center = center
                )
            }
        }

        // Static UI elements on top of the spinning disc
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Center Label Text (doesn't rotate)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = songTitle,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = artistName,
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            // Play/Pause Button in the very center
            androidx.compose.material3.IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = if (isPlaying) "⏸" else "▶",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

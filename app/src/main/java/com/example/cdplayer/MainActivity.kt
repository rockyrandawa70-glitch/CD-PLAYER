package com.example.cdplayer

import android.Manifest
import android.content.ContentUris
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class MainActivity : ComponentActivity() {

    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayer.Builder(this).build()

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            loadSongs()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        setContent {
            MaterialTheme {
                MusicPlayerUI(player)
            }
        }
    }

    private fun loadSongs() {

        val songs = mutableListOf<MediaItem>()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )

        val query = contentResolver.query(
            collection,
            projection,
            null,
            null,
            null
        )

        query?.use {

            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

            while (it.moveToNext()) {

                val id = it.getLong(idColumn)

                val uri = ContentUris.withAppendedId(collection, id)

                songs.add(MediaItem.fromUri(uri))
            }
        }

        player.setMediaItems(songs)
        player.prepare()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

@Composable
fun MusicPlayerUI(player: ExoPlayer) {

    var isPlaying by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->

                    if (dragAmount > 50) {
                        player.seekToPreviousMediaItem()
                    }

                    if (dragAmount < -50) {
                        player.seekToNextMediaItem()
                    }
                }
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "NOW PLAYING",
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier.size(320.dp),
                contentAlignment = Alignment.Center
            ) {

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.White,
                        startAngle = 120f,
                        sweepAngle = 220f,
                        useCenter = false,
                        style = Stroke(8f, cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(if (isPlaying) rotation else 0f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.LightGray,
                                    Color.DarkGray,
                                    Color.Black
                                )
                            ),
                            shape = CircleShape
                        )
                ) {

                    Canvas(modifier = Modifier.fillMaxSize()) {

                        for (i in 1..15) {

                            drawCircle(
                                color = Color.Black.copy(alpha = 0.2f),
                                radius = size.minDimension / 2 - i * 10,
                                style = Stroke(2f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Music From Device",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Minimal CD Experience",
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = {}) {
                    Icon(Icons.Default.QueueMusic, null, tint = Color.White)
                }

                IconButton(onClick = {
                    player.seekToPreviousMediaItem()
                }) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White)
                }

                FloatingActionButton(
                    onClick = {
                        isPlaying = !isPlaying

                        if (isPlaying) {
                            player.play()
                        } else {
                            player.pause()
                        }
                    },
                    containerColor = Color.White
                ) {
                    Icon(
                        if (isPlaying)
                            Icons.Default.Pause
                        else
                            Icons.Default.PlayArrow,
                        null,
                        tint = Color.Black
                    )
                }

                IconButton(onClick = {
                    player.seekToNextMediaItem()
                }) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White)
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.Shuffle, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

package com.example.gramophonemusicplayer.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramophonemusicplayer.data.MusicRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple manual dependency injection for this prototype
        val repository = MusicRepository(applicationContext)

        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf("main") }
                val viewModel: MainViewModel = viewModel {
                    MainViewModel(applicationContext, repository)
                }

                if (currentScreen == "main") {
                    MainScreen(viewModel) { currentScreen = "queue" }
                } else {
                    com.example.gramophonemusicplayer.ui.queue.QueueScreen(viewModel)
                }
            }
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, the ViewModel will automatically load songs
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel, onQueueClick: () -> Unit) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3E2723)) // Warm brown wooden tone
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { },
                    onDragCancel = { },
                    onDrag = { change, dragAmount ->
                        if (dragAmount.x > 50f) {
                            viewModel.playNext()
                        } else if (dragAmount.x < -50f) {
                            viewModel.playPrevious()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)
        ) {
            VinylDiscView(
                isPlaying = isPlaying,
                songTitle = currentSong?.title ?: "No Song Selected",
                artistName = currentSong?.artist ?: "Unknown Artist",
                onPlayPauseClick = { viewModel.togglePlayback() },
                onSeekForward = { viewModel.seekForward(30) },
                onSeekBackward = { viewModel.seekBackward(10) }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Timeline / Seek Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(progress),
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                Slider(
                    value = progress.toFloat(),
                    valueRange = 0f..totalDuration.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
                Text(
                    text = formatTime(totalDuration),
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.playPrevious() }) {
                    Text("⏮")
                }
                Button(onClick = { onQueueClick() }) {
                    Text("📋 Queue")
                }
                Button(onClick = { viewModel.shuffle() }) {
                    Text("🔀")
                }
                Button(onClick = { viewModel.playNext() }) {
                    Text("⏭")
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return "%02d:%02d".format(minutes, seconds)
}

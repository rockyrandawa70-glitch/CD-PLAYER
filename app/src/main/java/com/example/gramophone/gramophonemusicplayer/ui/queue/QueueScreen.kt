package com.example.gramophonemusicplayer.ui.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gramophonemusicplayer.data.MusicFile
import com.example.gramophonemusicplayer.ui.main.MainViewModel

@Composable
fun QueueScreen(viewModel: MainViewModel) {
    val queue by viewModel.repository.currentQueue.collectAsState()
    val currentIndex by viewModel.repository.currentSongIndex.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Playback Queue", color = Color.White) },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF3E2723)
                )
            )
        },
        containerColor = Color(0xFF3E2723)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(queue) { song ->
                QueueItem(
                    song = song,
                    isSelected = queue.indexOf(song) == currentIndex,
                    onClick = {
                        // Logic to play specific song from queue
                    }
                )
            }
        }
    }
}

@Composable
fun QueueItem(song: MusicFile, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = song.title,
                color = if (isSelected) Color.Yellow else Color.White,
                fontSize = 16.sp,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
            Text(
                text = song.artist,
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
        Text(
            text = formatTime(song.duration),
            color = Color.LightGray,
            fontSize = 14.sp
        )
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return "%02d:%02d".format(minutes, seconds)
}

package com.example.gramophonemusicplayer.data

import android.net.Uri

data class MusicFile(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumArtUri: Uri? = null,
    val dataPath: String? = null
)

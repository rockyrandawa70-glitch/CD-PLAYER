package com.example.gramophone.data

import android.net.Uri

data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumArtUri: Uri?
)

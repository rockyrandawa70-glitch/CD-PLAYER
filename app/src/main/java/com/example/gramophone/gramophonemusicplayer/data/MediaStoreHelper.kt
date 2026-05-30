package com.example.gramophonemusicplayer.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.Audio.Media.ALBUM_ID
import android.provider.MediaStore.Audio.Media.ARTIST
import android.provider.MediaStore.Audio.Media.DURATION
import android.provider.MediaStore.Audio.Media.TITLE
import android.provider.MediaStore.Audio.Media._ID
import android.provider.MediaStore.Audio.Media.DATA
import android.net.Uri

class MediaStoreHelper(private val context: Context) {

    fun fetchAllAudioFiles(): List<MusicFile> {
        val musicList = mutableListOf<MusicFile>()
        val projection = arrayOf(
            _ID,
            TITLE,
            ARTIST,
            DURATION,
            ALBUM_ID,
            DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${TITLE} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(_ID)
            val titleColumn = cursor.getColumnIndexOrThrow(TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(ALBUM_ID)
            val dataColumn = cursor.getColumnIndexOrThrow(DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val dataPath = cursor.getString(dataColumn)

                val contentUri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id)
                val albumArtUri = getAlbumArtUri(albumId)

                musicList.add(
                    MusicFile(
                        id = id,
                        uri = contentUri,
                        title = title,
                        artist = artist,
                        duration = duration,
                        albumArtUri = albumArtUri,
                        dataPath = dataPath
                    )
                )
            }
        }
        return musicList
    }

    private fun getAlbumArtUri(albumId: Long): Uri? {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }
}

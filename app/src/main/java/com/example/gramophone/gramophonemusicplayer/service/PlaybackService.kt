package com.example.gramophonemusicplayer.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build(),
                C.AUDIO_USAGE_DEFAULT
            )
            .build()

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controller: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        super.onDestroy()
    }

    // Helper to get player instance for the ViewModel
    companion object {
        fun getPlayer(context: android.content.Context): Player? {
            // In a real implementation, you'd connect to the session using MediaController
            // For simplicity in this prototype, we'll let the ViewModel use a MediaController
            return null
        }
    }
}

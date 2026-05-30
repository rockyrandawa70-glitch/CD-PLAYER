package com.example.gramophonemusicplayer.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.gramophonemusicplayer.data.MusicFile
import com.example.gramophonemusicplayer.data.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    applicationContext: Context,
    val repository: MusicRepository
) : ViewModel() {

    private var controller: MediaController? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<MusicFile?>(null)
    val currentSong: StateFlow<MusicFile?> = _currentSong.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    init {
        setupMediaController(applicationContext)
        repository.loadSongs()

        // Sync with repository
        viewModelScope.launch {
            repository.currentQueue.collect {
                updateCurrentSong()
            }
        }
    }

    private fun setupMediaController(context: Context) {
        val sessionToken = SessionToken(context, android.content.ComponentName(context, "com.example.gramophonemusicplayer.service.PlaybackService"))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            controller = controllerFuture.get()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    updateCurrentSong()
                }
            })

            // Start progress update timer
            viewModelScope.launch {
                while(true) {
                    kotlinx.coroutines.delay(1000)
                    _progress.value = controller?.currentPosition ?: 0L
                }
            }
        }, { it.run() })
    }

    private fun updateCurrentSong() {
        val song = repository.getCurrentSong()
        _currentSong.value = song
        _totalDuration.value = song?.duration ?: 0L
    }

    fun togglePlayback() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun playNext() {
        repository.nextSong()
        playCurrentSong()
    }

    fun playPrevious() {
        repository.previousSong()
        playCurrentSong()
    }

    private fun playCurrentSong() {
        val song = repository.getCurrentSong() ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .build()

        controller?.setMediaItem(mediaItem)
        controller?.prepare()
        controller?.play()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun seekForward(seconds: Int) {
        controller?.seekTo((controller?.currentPosition ?: 0L) + seconds * 1000)
    }

    fun seekBackward(seconds: Int) {
        controller?.seekTo(Math.max(0L, (controller?.currentPosition ?: 0L) - seconds * 1000))
    }

    fun shuffle() {
        repository.shuffleQueue()
        playCurrentSong()
    }
}

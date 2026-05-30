package com.example.gramophone.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramophone.data.MusicRepository
import com.example.gramophone.data.Song
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

class MusicViewModel(application: Application, private val repository: MusicRepository) : AndroidViewModel(application) {
    private val player: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _currentSongIndex = MutableStateFlow(-1)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex

    val currentSong: StateFlow<Song?> = _currentSongIndex.map { index ->
        if (index != -1) _songs.value[index] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration

    private var isShuffleMode = false

    init {
        loadSongs()
        setupPlayer()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            val fetchedSongs = repository.fetchAllSongs()
            _songs.value = fetchedSongs
            if (fetchedSongs.isNotEmpty()) {
                _currentSongIndex.value = 0
                playSong(0)
            }
        }
    }

    private fun setupPlayer() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                // Handle discontinuities if needed
            }
        })

        // Simple timer for progress updates
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _progress.value = player.currentPosition
                    _totalDuration.value = player.duration.coerceAtLeast(0L)
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun togglePlayback() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun playSong(index: Int) {
        if (index < 0 || index >= _songs.value.size) return
        _currentSongIndex.value = index
        val song = _songs.value[index]
        val mediaItem = MediaItem.fromUri(song.uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun playNext() {
        val nextIndex = _currentSongIndex.value + 1
        if (nextIndex < _songs.value.size) {
            playSong(nextIndex)
        } else if (isShuffleMode) {
            playSong((0 until _songs.value.size).random())
        } else {
            playSong(0)
        }
    }

    fun playPrevious() {
        val prevIndex = _currentSongIndex.value - 1
        if (prevIndex >= 0) {
            playSong(prevIndex)
        } else {
            playSong(_songs.value.size - 1)
        }
    }

    fun seekForward(seconds: Int) {
        player.seekTo(player.currentPosition + seconds * 1000L)
    }

    fun seekBackward(seconds: Int) {
        player.seekTo(player.currentPosition - seconds * 1000L)
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun shuffle() {
        isShuffleMode = !isShuffleMode
    }
}

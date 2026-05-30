package com.example.gramophonemusicplayer.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicRepository(context: Context) {
    private val mediaStoreHelper = MediaStoreHelper(context)

    private val _allSongs = MutableStateFlow<List<MusicFile>>(emptyList())
    val allSongs: StateFlow<List<MusicFile>> = _allSongs.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<MusicFile>>(emptyList())
    val currentQueue: StateFlow<List<MusicFile>> = _currentQueue.asStateFlow()

    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()

    fun loadSongs() {
        val songs = mediaStoreHelper.fetchAllAudioFiles()
        _allSongs.value = songs
        _currentQueue.value = songs // Default queue is all songs
    }

    fun setQueue(songs: List<MusicFile>) {
        _currentQueue.value = songs
        _currentSongIndex.value = 0
    }

    fun nextSong() {
        if (_currentQueue.value.isNotEmpty()) {
            _currentSongIndex.value = (_currentSongIndex.value + 1) % _currentQueue.value.size
        }
    }

    fun previousSong() {
        if (_currentQueue.value.isNotEmpty()) {
            _currentSongIndex.value = if (_currentSongIndex.value == 0) {
                _currentQueue.value.size - 1
            } else {
                _currentSongIndex.value - 1
            }
        }
    }

    fun shuffleQueue() {
        _currentQueue.value = _currentQueue.value.shuffled()
        _currentSongIndex.value = 0
    }

    fun getCurrentSong(): MusicFile? {
        return _currentQueue.value.getOrNull(_currentSongIndex.value)
    }
}

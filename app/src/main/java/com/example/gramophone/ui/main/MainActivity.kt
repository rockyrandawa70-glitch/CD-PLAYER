package com.example.gramophone.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gramophone.databinding.ActivityMainBinding
import com.example.gramophone.data.MusicRepository
import com.example.gramophone.ui.queue.QueueActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(this)
        setContentView(binding.root)

        val repository = MusicRepository(applicationContext)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.AndroidViewModelFactory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MusicViewModel(application, repository) as T
            }
        })[MusicViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnPlayPause.setOnClickListener {
            viewModel.togglePlayback()
        }

        binding.btnNext.setOnClickListener {
            viewModel.playNext()
        }

        binding.btnPrev.setOnClickListener {
            viewModel.playPrevious()
        }

        binding.btnShuffle.setOnClickListener {
            viewModel.shuffle()
        }

        binding.btnQueue.setOnClickListener {
            startActivity(Intent(this, QueueActivity::class.java))
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekTo(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.vinylDiscView.onSeekListener = { forward ->
            if (forward) viewModel.seekForward(30) else viewModel.seekBackward(10)
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (velocityX > 100) {
                    viewModel.playNext()
                    return true
                } else if (velocityX < -100) {
                    viewModel.playPrevious()
                    return true
                }
                return false
            }
        })

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onGestureEvent(event)
            false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                binding.vinylDiscView.setRotation(isPlaying)
                binding.btnPlayPause.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
            }
        }

        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                song?.let {
                    binding.vinylDiscView.setSongInfo(it.title, it.artist, it.albumArtUri)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.progress.collectLatest { progress ->
                binding.seekBar.progress = progress.toInt()
                binding.txtCurrentTime.text = formatTime(progress)
            }
        }

        lifecycleScope.launch {
            viewModel.totalDuration.collectLatest { duration ->
                binding.seekBar.max = duration.toInt()
                binding.txtTotalTime.text = formatTime(duration)
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}

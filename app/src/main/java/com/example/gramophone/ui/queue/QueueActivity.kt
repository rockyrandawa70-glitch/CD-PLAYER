package com.example.gramophone.ui.queue

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gramophone.databinding.ActivityQueueBinding
import com.example.gramophone.ui.main.MusicViewModel

class QueueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQueueBinding
    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueueBinding.inflate(this)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = SongAdapter(viewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
}

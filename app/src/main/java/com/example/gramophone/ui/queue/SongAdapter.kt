package com.example.gramophone.ui.queue

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gramophone.databinding.ItemSongBinding
import com.example.gramophone.ui.main.MusicViewModel

class SongAdapter(private val viewModel: MusicViewModel) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var songs = listOf<com.example.gramophone.data.Song>()

    fun updateSongs(newSongs: List<com.example.gramophone.data.Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.binding.txtTitle.text = song.title
        holder.binding.txtArtist.text = song.artist
        holder.itemView.setOnClickListener {
            viewModel.playSong(position)
        }
    }

    override fun getItemCount(): Int = songs.size

    class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root)
}

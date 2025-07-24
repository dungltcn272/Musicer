package com.ltcn272.musicer.screen.main.assets.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ltcn272.musicer.R
import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.databinding.ItemMusicBinding

class SongAdapter : ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback()) {

    inner class SongViewHolder(private val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.tvItemTitle.text = song.title
            binding.tvItemArtist.text = song.artist

            if (song.thumbnail.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(song.thumbnail)
                    .placeholder(R.drawable.ic_default_album_art)
                    .into(binding.ivItemAlbumArt)
            } else {
                binding.ivItemAlbumArt.setImageResource(R.drawable.ic_default_album_art)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem
    }
}


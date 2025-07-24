package com.ltcn272.musicer.screen.main.assets.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ltcn272.musicer.R
import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.databinding.ItemMusicBinding
import com.ltcn272.musicer.screen.play_music.PlayMusicActivity

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

            binding.root.setOnClickListener {
                val index = adapterPosition
                if (index != RecyclerView.NO_POSITION) {
                    val intent = Intent(itemView.context, PlayMusicActivity::class.java).apply {
                        putExtra("song_title", song.title)
                        putExtra("song_artist", song.artist)
                        putExtra("song_path", song.audioUrl)
                        putParcelableArrayListExtra("SONG_LIST", ArrayList(currentList))
                        putExtra("SONG_INDEX", index)
                    }
                    itemView.context.startActivity(intent)
                }
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


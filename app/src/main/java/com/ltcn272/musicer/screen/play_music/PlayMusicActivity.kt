package com.ltcn272.musicer.screen.play_music

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ltcn272.musicer.R
import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.databinding.ActivityPlayMusicBinding
import com.ltcn272.musicer.service.MusicService
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.Glide

class PlayMusicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayMusicBinding
    private var musicService: MusicService? = null
    private var isBound = false
    private var isUserSeeking = false
    private var currentSong: Song? = null
    private var isPlaying = false

    private val songChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra("SONG_OBJECT", Song::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra("SONG_OBJECT")
            }
            song?.let {
                currentSong = it
                updateUI(it)
            }
        }
    }

    private val progressUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra("PROGRESS", 0) ?: 0
            val duration = intent?.getIntExtra("DURATION", 0) ?: 0
            if (!isUserSeeking) {
                binding.seekBarMusic.progress = progress
                binding.tvCurrentTime.text = formatDuration(progress)
                binding.seekBarMusic.max = duration
                binding.tvDuration.text = formatDuration(duration)
            }
        }
    }


    private val errorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val errorMessage = intent?.getStringExtra("ERROR_MESSAGE") ?: "Đã xảy ra lỗi"
            Toast.makeText(this@PlayMusicActivity, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private val musicStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isPlayingNow = intent?.getBooleanExtra("IS_PLAYING", false) ?: false

            isPlaying = isPlayingNow

            changePlayPauseVisibility()
        }
    }

    private val statusChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val statusType = intent?.getStringExtra("STATUS_TYPE")
            val enabled = intent?.getBooleanExtra("ENABLED", false) ?: false
//            when (statusType) {
//                "Shuffle" -> binding.btnShuffle.setImageResource(
//                    if (enabled) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle_off
//                )
//                "Repeat" -> binding.btnRepeat.setImageResource(
//                    if (enabled) R.drawable.ic_repeat_on else R.drawable.ic_repeat_off
//                )
//            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.getCurrentSong()?.let { updateUI(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)


        setupControlButtons()

        setupSeekBar()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val songIntentFilter = IntentFilter("com.ltcn272.musicer.SONG_CHANGED")
        val progressIntentFilter = IntentFilter("com.ltcn272.musicer.PROGRESS_UPDATE")
        val errorIntentFilter = IntentFilter("com.ltcn272.musicer.ERROR")
        val statusIntentFilter = IntentFilter("com.ltcn272.musicer.STATUS_CHANGED")
        val playingFilter = IntentFilter("com.ltcn272.musicer.MUSIC_STATE_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+: Sử dụng RECEIVER_NOT_EXPORTED
            ContextCompat.registerReceiver(
                this,
                songChangedReceiver,
                songIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            ContextCompat.registerReceiver(
                this,
                progressUpdateReceiver,
                progressIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            ContextCompat.registerReceiver(
                this,
                errorReceiver,
                errorIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            ContextCompat.registerReceiver(
                this,
                statusChangedReceiver,
                statusIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            ContextCompat.registerReceiver(
                this, musicStateReceiver, playingFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } else {
            // API < 33: Đăng ký không cần cờ
            registerReceiver(songChangedReceiver, songIntentFilter)
            registerReceiver(progressUpdateReceiver, progressIntentFilter)
            registerReceiver(errorReceiver, errorIntentFilter)
            registerReceiver(statusChangedReceiver, statusIntentFilter)
            registerReceiver(musicStateReceiver, playingFilter)
        }
    }

    private fun setupControlButtons() {
        binding.ivPlayPause.setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_PLAY
            }
            startService(intent)
            changePlayPauseVisibility()
        }

        binding.ivNext.setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_NEXT
            }
            startService(intent)
        }

        binding.ivPrev.setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_PREV
            }
            startService(intent)
        }

//        binding.btnShuffle.setOnClickListener {
//            musicService?.toggleShuffleMode()
//        }
//
//        binding.btnRepeat.setOnClickListener {
//            musicService?.toggleRepeatMode()
//        }
    }

    private fun changePlayPauseVisibility() {
        binding.ivPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun setupSeekBar() {
        binding.seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTime.text = formatDuration(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                val intent = Intent(this@PlayMusicActivity, MusicService::class.java).apply {
                    action = MusicService.SEEK_TO
                    putExtra("SEEK_POSITION", seekBar?.progress ?: 0)
                }
                startService(intent)
            }
        })
    }

    private fun updateUI(song: Song) {
        currentSong = song
        binding.tvMusicTitle.text = song.title
        binding.tvMusicTitle.isSelected = true
        binding.tvMusicArtist.text = song.artist
        binding.seekBarMusic.max = song.duration
        binding.tvDuration.text = formatDuration(song.duration)
        musicService?.getCurrentTime()?.let {
            binding.seekBarMusic.progress = it
            binding.tvCurrentTime.text= formatDuration(it)
        }
        if (song.thumbnail.isNotEmpty()) {
            Glide.with(binding.root.context)
                .load(song.thumbnail)
                .placeholder(R.drawable.ic_default_album_art)
                .into(binding.ivAlbumArt)
        } else {
            binding.ivAlbumArt.setImageResource(R.drawable.ic_default_album_art)
        }
        binding.ivPlayPause.setImageResource(
            if (musicService?.isPlaying() == true) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

//    private fun updateStatusButtons() {
//        binding.btnShuffle.setImageResource(
//            if (musicService?.isShuffle == true) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle_off
//        )
//        binding.btnRepeat.setImageResource(
//            if (musicService?.isRepeat == true) R.drawable.ic_repeat_on else R.drawable.ic_repeat_off
//        )
//    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        unregisterReceiver(songChangedReceiver)
        unregisterReceiver(progressUpdateReceiver)
        unregisterReceiver(errorReceiver)
        unregisterReceiver(statusChangedReceiver)
        unregisterReceiver(musicStateReceiver)
    }

}
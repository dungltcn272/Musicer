package com.ltcn272.musicer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.ltcn272.musicer.R
import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.receiver.NotificationReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var notificationManager: NotificationManagerCompat
    private val CHANNEL_ID = "music_channel"
    private var isPlaying = false
    private var songList: List<Song>? = null
    private var currentPosition = 0
    private var isShuffle = false
    private var isRepeat = false
    private var progressJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tempNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle("Đang chuẩn bị phát nhạc...")
            .setContentText("Vui lòng đợi")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, tempNotification)

        intent?.let {
            val receiveSongList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("SONG_LIST", Song::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("SONG_LIST")
            }

            if (!receiveSongList.isNullOrEmpty()) {
                songList = receiveSongList
            } else {
                Log.w("MusicService", "Song list is null or empty.")
            }

            val receivePosition = it.getIntExtra("SONG_INDEX", -1)
            if (receivePosition != -1) currentPosition = receivePosition

            when (it.action) {
                null -> {
                    if (!songList.isNullOrEmpty()) {
                        playSong(songList!![currentPosition])
                    } else {
                        stopSelf()
                    }
                }
                ACTION_PLAY -> togglePlayPause()
                ACTION_NEXT -> playNext()
                ACTION_PREV -> playPrevious()
                SEEK_TO -> {
                    val seekPosition = intent.getIntExtra("SEEK_POSITION", 0)
                    mediaPlayer?.seekTo(seekPosition)
                    showNotification(songList?.getOrNull(currentPosition))
                }
                ACTION_SHUFFLE -> toggleShuffle()
                ACTION_REPEAT -> toggleRepeat()
            }
        }

        return START_STICKY
    }

    private fun playSong(song: Song, retryCount: Int = 0) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        val attributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        mediaPlayer?.apply {
            setAudioAttributes(attributes)
            try {
                if (song.audioUrl.startsWith("http")) {
                    setDataSource(song.audioUrl)
                } else {
                    setDataSource(applicationContext, song.audioUrl.toUri())
                }
                setOnPreparedListener {
                    it.start()
                    this@MusicService.isPlaying = true
                    showNotification(song)
                    notifyMusicStateChanged(song, true)
                    startProgressUpdates()
                }
                setOnCompletionListener {
                    if (isRepeat) {
                        playSong(song)
                    } else {
                        playNext()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("MusicService", "MediaPlayer error: what=$what, extra=$extra")
                    if (song.audioUrl.startsWith("http") && retryCount < 3) {
                        playSong(song, retryCount + 1)
                    } else {
                        notifyError("Lỗi phát nhạc: $what")
                        stopSelf()
                    }
                    true
                }
                prepareAsync()
            } catch (e: Exception) {
                Log.e("MusicService", "Error setting data source", e)
                if (song.audioUrl.startsWith("http") && retryCount < 3) {
                    playSong(song, retryCount + 1)
                } else {
                    notifyError("Không thể phát bài hát: ${e.message}")
                    stopSelf()
                }
            }
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
                stopProgressUpdates()
            } else {
                it.start()
                isPlaying = true
                startProgressUpdates()
            }
            showNotification(songList?.getOrNull(currentPosition))
            notifyMusicStateChanged(songList?.getOrNull(currentPosition), isPlaying)
        }
    }

    private fun playNext() {
        if (songList.isNullOrEmpty()) {
            Log.w("MusicService", "Song list is empty, cannot play next")
            return
        }
        currentPosition = if (isShuffle) {
            (songList!!.indices).random()
        } else {
            (currentPosition + 1) % songList!!.size
        }
        playSong(songList!![currentPosition])
        notifySongChanged(songList!![currentPosition])
    }

    private fun playPrevious() {
        if (songList.isNullOrEmpty()) {
            Log.w("MusicService", "Song list is empty, cannot play previous")
            return
        }
        currentPosition = if (isShuffle) {
            (songList!!.indices).random()
        } else {
            if (currentPosition - 1 < 0) songList!!.size - 1 else currentPosition - 1
        }
        playSong(songList!![currentPosition])
        notifySongChanged(songList!![currentPosition])
    }

    private fun toggleShuffle() {
        isShuffle = !isShuffle
        notifyStatusChanged("Shuffle", isShuffle)
    }

    private fun toggleRepeat() {
        isRepeat = !isRepeat
        notifyStatusChanged("Repeat", isRepeat)
    }

    private fun showNotification(song: Song?) {
        val title = song?.title ?: "Không có bài hát"
        val artist = song?.artist ?: "Unknown"

        val thumbnailBitmap = song?.thumbnail?.let { thumbnail ->
            try {
                when {
                    thumbnail.startsWith("http", true) || thumbnail.startsWith("https", true) -> {
                        Glide.with(this)
                            .asBitmap()
                            .load(thumbnail)
                            .submit(128, 128)
                            .get()
                    }
                    thumbnail.startsWith("content://", true) || thumbnail.startsWith("file://", true) -> {
                        val uri = thumbnail.toUri()
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    }
                    else -> {
                        BitmapFactory.decodeFile(thumbnail)
                    }
                }
            } catch (e: Exception) {
                null
            }
        } ?: BitmapFactory.decodeResource(resources, R.drawable.ic_default_album_art)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(thumbnailBitmap)
            .addAction(R.drawable.ic_prev, "Prev", getPendingIntent(ACTION_PREV))
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                getPendingIntent(ACTION_PLAY)
            )
            .addAction(R.drawable.ic_next, "Next", getPendingIntent(ACTION_NEXT))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .build()

        startForeground(1, notification)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isPlaying) {
                mediaPlayer?.let {
                    val progress = it.currentPosition
                    val duration = it.duration
                    notifyProgressUpdate(progress, duration)
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun notifyProgressUpdate(progress: Int, duration: Int) {
        val intent = Intent("com.ltcn272.musicer.PROGRESS_UPDATE")
        intent.putExtra("PROGRESS", progress)
        intent.putExtra("DURATION", duration)
        sendBroadcast(intent)
    }

    private fun notifySongChanged(song: Song) {
        val intent = Intent("com.ltcn272.musicer.SONG_CHANGED")
        intent.putExtra("SONG_OBJECT", song)
        sendBroadcast(intent)
    }

    private fun notifyStatusChanged(status: String, enabled: Boolean) {
        val intent = Intent("com.ltcn272.musicer.STATUS_CHANGED")
        intent.putExtra("STATUS_TYPE", status)
        intent.putExtra("ENABLED", enabled)
        sendBroadcast(intent)
    }

    private fun notifyMusicStateChanged(song: Song?, isPlaying: Boolean) {
        val intent = Intent("com.ltcn272.musicer.MUSIC_STATE_CHANGED").apply {
            putExtra("IS_PLAYING", isPlaying)
            putExtra("SONG_NAME", song?.title ?: "Không xác định")
        }
        sendBroadcast(intent)
    }

    private fun notifyError(message: String) {
        val intent = Intent("com.ltcn272.musicer.ERROR")
        intent.putExtra("ERROR_MESSAGE", message)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        notifyMusicStateChanged(null, false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("MusicService", "onBind called")
        return MusicBinder()
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getCurrentSong(): Song? = songList?.getOrNull(currentPosition)
    fun isPlaying(): Boolean = isPlaying
    fun toggleShuffleMode() = toggleShuffle()
    fun toggleRepeatMode() = toggleRepeat()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val SEEK_TO = "SEEK_TO"
        const val ACTION_SHUFFLE = "ACTION_SHUFFLE"
        const val ACTION_REPEAT = "ACTION_REPEAT"
    }
}
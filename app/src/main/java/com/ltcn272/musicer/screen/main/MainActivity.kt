package com.ltcn272.musicer.screen.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ui.setupWithNavController
import com.ltcn272.musicer.R
import dagger.hilt.android.AndroidEntryPoint

import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ltcn272.musicer.screen.play_music.PlayMusicActivity
import com.ltcn272.musicer.service.MusicService

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var miniPlayer: LinearLayout
    private lateinit var tvSongName: TextView
    private lateinit var btnPlayPause: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnPrev: ImageView

    private var isPlaying = false

    private val musicStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isPlayingNow = intent?.getBooleanExtra("IS_PLAYING", false) ?: false
            val songName = intent?.getStringExtra("SONG_NAME") ?: "Không có bài hát"

            isPlaying = isPlayingNow
            miniPlayer.visibility = if (songName != "Không có bài hát") View.VISIBLE else View.GONE
            tvSongName.text = songName

            btnPlayPause.setImageResource(
                if (isPlayingNow) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        requestNotificationPermissionIfNeeded()

        setView()
        setListener()

        val intentFilter = IntentFilter("com.ltcn272.musicer.MUSIC_STATE_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                this, musicStateReceiver, intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(musicStateReceiver, intentFilter)
        }

    }

    private fun setView() {
        miniPlayer = findViewById(R.id.mini_player)
        tvSongName = findViewById(R.id.tv_song_name)
        btnPlayPause = findViewById(R.id.btn_pause_resume)
        btnNext = findViewById(R.id.btn_next)
        btnPrev = findViewById(R.id.btn_prev)
    }

    private fun setListener() {
        miniPlayer.setOnClickListener{
            val intent = Intent(this, PlayMusicActivity::class.java)
            startActivity(intent)
        }
        btnPlayPause.setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                this.action = "ACTION_PLAY"
            }
            startService(intent)
        }

        btnNext.setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                action = "ACTION_NEXT"
            }
            startService(intent)
        }

        btnPrev.setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                action = "ACTION_PREV"
            }
            startService(intent)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Quyền đã được cấp
            } else {
                // Người dùng từ chối
            }
        }

}
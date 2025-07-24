package com.ltcn272.musicer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.ltcn272.musicer.service.MusicService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        val serviceIntent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}

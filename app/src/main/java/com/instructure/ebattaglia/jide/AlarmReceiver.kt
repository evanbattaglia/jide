package com.instructure.ebattaglia.jide

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("AlarmReceiver", "Jide setting wallpaper!!!");
        Toast.makeText(context, "Jide setting wallpaper!", Toast.LENGTH_LONG).show()
        WallpaperSetter.setWallpaper(context!!)
    }
}
package com.instructure.ebattaglia.jide

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("AlarmReceiver", "let's rock this party");
        // Put here YOUR code.
        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show() // I don't think this actually works
        WallpaperSetter.setWallpaper(context!!)
    }
}
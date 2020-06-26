package com.instructure.ebattaglia.jide

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"

        fun setAlarm(context: Context, frequencyMinutes: Int) {
            Log.d(TAG, "setting up alarm, frequency=$frequencyMinutes minutes")
            val intent = Intent(context, AlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(context, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // make interval a lot bigger
            val interval = frequencyMinutes * 60000L
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5, interval, pi)
        }

    }
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("AlarmReceiver", "Jide setting wallpaper!!!");
        Toast.makeText(context, "Jide setting wallpaper!", Toast.LENGTH_LONG).show()
        WallpaperSetter.setWallpaper(context!!)
    }
}
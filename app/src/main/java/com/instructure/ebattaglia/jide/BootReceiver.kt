package com.instructure.ebattaglia.jide

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    companion object {
        // TODO disable as well of course!
        fun enable(context: Context) {
            val receiver = ComponentName(context, BootReceiver::class.java)
            context.packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("BootReceive", "Booting up, setting alarm to change wallpaper")
        Toast.makeText(context, "Jide setting alarm for wallpaper!", Toast.LENGTH_LONG).show()
        AlarmReceiver.setAlarm(context!!, JideWallpaperPreferences(context).frequencyMinutes())
    }
}
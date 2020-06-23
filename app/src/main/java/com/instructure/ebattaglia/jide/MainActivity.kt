package com.instructure.ebattaglia.jide

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val GET_PERMISSIONS = 0
    }

    fun setAlarm() {
        val context = applicationContext
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // make interval a lot bigger
        val interval = 1 * /*hour*/ 60 * /*minute*/ 60000L
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5, interval, pi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setAlarm()

        Log.d("MainActivity", "Kicked off alarm")

        // Button to set it now
        button.setOnClickListener {
            doit()
        }
    }

    //I have the permission now, not sure if still need to request it
    fun doit() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            // may atually need write???
            ), GET_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            GET_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    WallpaperSetter.setWallpaper(applicationContext)
                    setAlarm()
                } else {
                    Toast.makeText(this, "Need permissions", Toast.LENGTH_LONG)
                }
            }
        }
    }

}

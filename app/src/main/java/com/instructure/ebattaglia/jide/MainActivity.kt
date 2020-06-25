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
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val GET_PERMISSIONS = 0
        const val GET_ANKI_PERMISSIONS = 1
    }

    fun setAlarm() {
        Log.d("MainActivity", "setup alarm")
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

        // Button to set it now
        run_button.setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GET_PERMISSIONS)
        }

        /*
        TODO for when we show list of decks here
        testButton.setOnClickListener {
            // TODO need to put this somewhere
            requestPermissions(arrayOf("com.ichi2.anki.permission.READ_WRITE_DATABASE"), GET_ANKI_PERMISSIONS)
        }
         */
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
            /*
            TODO for when we show list of decks here
            GET_ANKI_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    getAnkiDeck()
                } else {
                    Toast.makeText(this, "Need permissions, you", Toast.LENGTH_LONG)
                }
            }
             */
        }
    }
}

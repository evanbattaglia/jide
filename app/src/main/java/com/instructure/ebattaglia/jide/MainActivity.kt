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
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import androidx.preference.PreferenceManager
import android.os.Environment.getExternalStorageDirectory
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import android.os.Environment
import android.widget.Toast
import java.io.File


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val PICK_ANKI_DB_FILE = 1111
    }

    fun setAlarm() {
        val context = applicationContext
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // TODO much later: set to setInexactRepeating() (although it is already pretty inexact) and
        // make interval a lot bigger
        val interval = 10000
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5, 10000, pi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setAlarm()

        Log.d("MainActivity", "Kicked off alarm")

        // Button to set it now
        button.setOnClickListener {
            list()
        }
    }

/*

    fun doit2() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val uriStr = prefs.getString(getString(R.string.preferences_anki_uri), null)
        if (uriStr != null) {
            val uri = Uri.parse(uriStr)
            Log.e("URI IS", "uri is ${uri.path}")
            val card = Anki.getCard("/storage/emulated/0/AnkiDroid/collection.anki2")
            WallpaperSetter.setWallpaper(applicationContext, card[0], card[1])
            Log.d("YEAH YEAH", "hey ${card[0]}, ${card[1]}")
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/x-sqlite3"
            }

            startActivityForResult(intent, PICK_ANKI_DB_FILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_ANKI_DB_FILE && resultCode == Activity.RESULT_OK) {
            val uri = data!!.data!!

            val card = Anki.getCard(application, uri)
            Log.d("YEAH YEAH", "hey ${card[0]}, ${card[1]}")
            WallpaperSetter.setWallpaper(applicationContext, card[0], card[1])

            val prefsEdit = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
            prefsEdit.putString(getString(R.string.preferences_anki_uri), uri.toString())
            prefsEdit.commit()
        }
    }



    //I have the permission now, not sure if still need to request it
    fun doit() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), 12345
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            12345 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    //Anki.getCard("")
                    Log.d(
                        "UGG",
                        Environment.getExternalStorageDirectory().listFiles().joinToString(",")
                    )
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

*/
    fun list() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
    } else {
        listExternalStorage()
    }
}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                listExternalStorage()
            } else {
                Toast.makeText(this, "Until you grant the permission, I cannot list the files", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun listExternalStorage() {
        Log.d(
            "UGG",
            Environment.getExternalStorageDirectory().listFiles().joinToString(",")
        )
    }


}

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
import android.widget.ArrayAdapter
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val GET_PERMISSIONS = 0
        const val GET_ANKI_PERMISSIONS = 1
    }

    fun setAlarm(frequencyMinutes: Int) {
        Log.d(TAG, "setting up alarm, frequency=$frequencyMinutes minutes")
        val context = applicationContext
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // make interval a lot bigger
        val interval = frequencyMinutes * 60000L
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5, interval, pi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button to set it now
        run_button.setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GET_PERMISSIONS)
        }

        // TODO check first if really need permissions
        requestPermissions(arrayOf("com.ichi2.anki.permission.READ_WRITE_DATABASE"), GET_ANKI_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            GET_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Click "setup" button
                    val frequencyMinutes = wallpaper_frequency.text.toString().toIntOrNull() ?: JideWallpaperPreferences.DEFAULT_FREQUENCY_MINUTES
                    savePreferences(frequencyMinutes)
                    WallpaperSetter.setWallpaper(applicationContext)
                    setAlarm(frequencyMinutes)
                } else {
                    Toast.makeText(this, "Need permissions", Toast.LENGTH_LONG).show()
                }
            }
            GET_ANKI_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Activity startup
                    val decks : List<AnkiApi.Deck> = AnkiApi.getAllDecks(this)
                    val spinnerAdapter = ArrayAdapter<AnkiApi.Deck>(this, android.R.layout.simple_spinner_dropdown_item, decks)
                    wallpaper_deck_spinner.adapter = spinnerAdapter

                    // TODO: the right way with ViewModel, yada yada yada
                    val prefs = JideWallpaperPreferences(applicationContext)
                    wallpaper_lockscreen_firstfield.setText(prefs.lockscreenFirstField())
                    wallpaper_lockscreen_secondfield.setText(prefs.lockscreenSecondField())
                    wallpaper_launcher_firstfield.setText(prefs.launcherFirstField())
                    wallpaper_launcher_secondfield.setText(prefs.launcherSecondField())
                    wallpaper_same_lockscreen_launcher_checkbox.isChecked = prefs.lockscreenLauncherSame()
                    wallpaper_frequency.setText(prefs.frequencyMinutes().toString())
                    val deckId = prefs.deckId()
                    wallpaper_deck_spinner.setSelection(decks.indexOfFirst { it.id == deckId })
                } else {
                    Toast.makeText(this, "Need Anki permissions to get Deck list", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun savePreferences(frequencyMinutes: Int) {
        val deck = wallpaper_deck_spinner.selectedItem as AnkiApi.Deck
        JideWallpaperPreferences(applicationContext).set(
            deckId=deck.id,
            lockscreenFirstField=wallpaper_lockscreen_firstfield.text.toString(),
            lockscreenSecondField=wallpaper_lockscreen_secondfield.text.toString(),
            lockscreenLauncherSame=wallpaper_same_lockscreen_launcher_checkbox.isChecked,
            launcherFirstField=wallpaper_launcher_firstfield.text.toString(),
            launcherSecondField=wallpaper_launcher_secondfield.text.toString(),
            frequencyMinutes=frequencyMinutes
        )
    }
}

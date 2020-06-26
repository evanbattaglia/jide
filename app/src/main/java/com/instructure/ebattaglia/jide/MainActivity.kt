package com.instructure.ebattaglia.jide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.widget.ArrayAdapter
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val GET_PERMISSIONS = 0
        const val GET_ANKI_PERMISSIONS = 1
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
                    BootReceiver.enable(applicationContext)
                    AlarmReceiver.setAlarm(applicationContext, frequencyMinutes)
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

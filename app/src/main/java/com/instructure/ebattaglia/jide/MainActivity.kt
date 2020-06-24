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
import android.net.Uri
import android.widget.Toast
import com.ichi2.anki.FlashCardsContract

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val GET_PERMISSIONS = 0
        const val GET_ANKI_PERMISSIONS = 1
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

        testButton.setOnClickListener {
            requestPermissions(arrayOf("com.ichi2.anki.permission.READ_WRITE_DATABASE"), GET_ANKI_PERMISSIONS)
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
        }
    }

    private fun getAnkiDeck() {
        val cursor = contentResolver.query(FlashCardsContract.Deck.CONTENT_SELECTED_URI, null, null, null, null)
        cursor.moveToFirst()
        for (i in 0 until cursor!!.columnCount) {
            Log.e(
                "column types, go",
                "${i} ${cursor.getColumnName(i)} TYPE=${cursor.getType(i)}"
            )
        }
        Log.e("deck name, go", cursor.getString(cursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME)))
        cursor.close()

        val cursor2 = contentResolver.query(FlashCardsContract.ReviewInfo.CONTENT_URI, null, "limit=?,deckId=?", arrayOf("1", "1521405849755"), null)
        cursor2.moveToFirst()
        for (i in 0 until cursor2.columnCount) {
            Log.e(
                "column types2, go",
                "${i} ${cursor2.getColumnName(i)} TYPE=${cursor2.getType(i)}"
            )
        }
        val noteId = cursor2.getLong(cursor2.getColumnIndex(FlashCardsContract.ReviewInfo.NOTE_ID))
        val ordId = cursor2.getLong(cursor2.getColumnIndex(FlashCardsContract.ReviewInfo.CARD_ORD))
        cursor2.close()

        val cursor3 = contentResolver.query(Uri.parse("${FlashCardsContract.Note.CONTENT_URI}/${noteId}/cards/${ordId}"),
            arrayOf(FlashCardsContract.Card.ANSWER_PURE, FlashCardsContract.Card.QUESTION_SIMPLE), null, null, null)
        cursor3.moveToFirst()
        for (i in 0 until cursor3.columnCount) {
            Log.e(
                "column types3, go",

                "${i} ${cursor3.getColumnName(i)} TYPE=${cursor3.getType(i)}"
            )
        }
        Log.e("question!!!", cursor3.getString(cursor3.getColumnIndex(FlashCardsContract.Card.QUESTION_SIMPLE)))
        Log.e("answer pure!!!", cursor3.getString(cursor3.getColumnIndex(FlashCardsContract.Card.ANSWER_PURE)))
        cursor3.close()

        // TODO In Anki can we have different templates for different mediums that are still tracked the same for learning purposes as the same cards in the same deck?
        // Or, maybe we just have an "ord override" in our app (that gets weird with multiple decks though)


    }

}

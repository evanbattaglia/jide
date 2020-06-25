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
import android.app.WallpaperManager
import android.graphics.*
import android.webkit.WebView
import android.view.View
import android.graphics.Bitmap
import android.text.Html
import android.text.StaticLayout
import android.text.TextPaint
import android.text.Layout


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
            // TODO need to put this somewhere
            requestPermissions(arrayOf("com.ichi2.anki.permission.READ_WRITE_DATABASE"), GET_ANKI_PERMISSIONS)
        }
    }

    //I have the permission now, not sure if still need to request it
    fun doit() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GET_PERMISSIONS)
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
            arrayOf(FlashCardsContract.Card.ANSWER, FlashCardsContract.Card.QUESTION, FlashCardsContract.Card.ANSWER_PURE, FlashCardsContract.Card.QUESTION_SIMPLE), null, null, null)
        cursor3.moveToFirst()
        for (i in 0 until cursor3.columnCount) {
            Log.e(
                "column types3, go",

                "${i} ${cursor3.getColumnName(i)} TYPE=${cursor3.getType(i)}"
            )
        }
        Log.e("question!!!", cursor3.getString(cursor3.getColumnIndex(FlashCardsContract.Card.QUESTION_SIMPLE)))
        Log.e("answer pure!!!", cursor3.getString(cursor3.getColumnIndex(FlashCardsContract.Card.ANSWER_PURE)))

        // TODO In Anki can we have different templates for different mediums that are still tracked the same for learning purposes as the same cards in the same deck?
        // Or, maybe we just have an "ord override" in our app (that gets weird with multiple decks though)

        screenshot3(cursor3.getString(cursor3.getColumnIndex(FlashCardsContract.Card.ANSWER_PURE)))
        cursor3.close()

    }

    private fun screenshot3(html: String) {
        val spanned = Html.fromHtml(html, null, null)
        val paint = TextPaint()
        paint.setTextSize(20f);
        paint.setColor(Color.RED);
        val sl = StaticLayout(spanned, paint, 1080, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
        val bitmap = Bitmap.createBitmap(
            1080, 2220, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        sl.draw(canvas)
        val wm = WallpaperManager.getInstance(applicationContext)
        wm.setBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), true, WallpaperManager.FLAG_LOCK)
    }

    private fun screenshot2(html: String) {
        val view = WebView(this)
        val scale = 2.0f
        val contentWidth = 240


        view.setPictureListener(object: WebView.PictureListener {
            override fun onNewPicture(view: WebView, picture: Picture?) {
                if (view.getProgress() == 100 && view.contentHeight > 0) {
                    view.setPictureListener(null)
                    // Content is now fully rendered
                    val width = Math.round(contentWidth * scale)
                    val height = Math.round(view.getContentHeight() * scale)
                    val bitmap = getBitmap(view, width, height)
                    // Display or print bitmap...
                    val wm = WallpaperManager.getInstance(applicationContext)
                    wm.setBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), true, WallpaperManager.FLAG_LOCK)
                }
            }

            private fun getBitmap(
                view: WebView, width: Int, height: Int
            ): Bitmap {
                val bitmap = Bitmap.createBitmap(
                    width, height, Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                view.draw(canvas)

                return bitmap
            }
        });

        view.loadData(html, "text/html", "UTF8")
        //view.capturePicture().draw()

        //view.setInitialScale(Math.round(scale * 100));
        // Width and height must be at least 1
        view.layout(0, 0, 1080, 2220);
    }

    private fun screenshot() {
        val webView = WebView(this)
        webView.loadData("<html><body><h1>Hello world</h1></body></html>", "text/html", "UTF8")
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        webView.layout(
            0, 0, webView.measuredWidth,
            webView.measuredHeight
        )
        webView.isDrawingCacheEnabled = true
        webView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            webView.measuredWidth,
            webView.measuredHeight, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        val paint = Paint()
        val iHeight = bitmap.height.toFloat()
        canvas.drawBitmap(bitmap, 0f, iHeight, paint)
        webView.draw(canvas)
        val wm = WallpaperManager.getInstance(this)
        wm.setBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), true, WallpaperManager.FLAG_LOCK)
    }

}

package com.instructure.ebattaglia.jide

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_LOCK
import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    companion object {
        // TODO move this stuff
        private fun drawTextOnCanvas(canvas: Canvas, text: String, textColor: Int, gravity: Int) {
            val paint = Paint(ANTI_ALIAS_FLAG)
            var testTextSize = 100f
            paint.textSize = testTextSize
            paint.textAlign = Paint.Align.LEFT
            var baseline = -paint.ascent() // ascent() is negative
            val neededWidth = (paint.measureText(text) + 0.5f).toInt()
            val neededHeight = (baseline + paint.descent() + 0.5f).toInt()
            // Adjust textsize to fit width/height
            paint.textSize = testTextSize * Math.min(canvas.width / neededWidth, canvas.height / neededHeight)
            paint.color =  textColor

            if (gravity == Gravity.BOTTOM) {
                canvas.drawText(text, 0f, canvas.height - paint.descent(), paint)
            } else {
                // TOP
                canvas.drawText(text,0f, -paint.ascent(), paint)
            }
        }

        private fun textAsBitmap(context: Context, textUpper: String, textBottom: String) : Bitmap {
            val metrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            drawTextOnCanvas(canvas, textUpper, Color.rgb(255,200,200), Gravity.TOP)
            drawTextOnCanvas(canvas, textBottom, Color.rgb(200,200,255), Gravity.BOTTOM)
            return image
        }

        fun setWallpaper(context: Context?) {
            Log.e("SETWALLPAPER", "VE SET ZE VALLPAPIR!!!");
            val wm = WallpaperManager.getInstance(context)
            val bitmap = textAsBitmap(context!!, Random.nextInt(0, 10000).toString(), "饺子")
            wm.setBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), true, FLAG_LOCK)
            Toast.makeText(context, "hi", Toast.LENGTH_SHORT).show()
        }

        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context = applicationContext
        var alarmMgr: AlarmManager? = null
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.d("MainActivity", "Kicking off 1234!")

        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 13000, pi)

        /*
        // TODO much later: set to setInexactRepeating() (although it is already pretty inexact) and
        // make interval a lot bigger
        //alarmMgr?.setInexactRepeating(
        alarmMgr!!.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 5,
            10000,
            pi
        )
*/
        Log.d("MainActivity", "Kicking off!")

        //button.setOnClickListener { setWallpaper(this.applicationContext) }
    }
}

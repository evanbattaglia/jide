package com.instructure.ebattaglia.jide

import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_LOCK
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.DisplayMetrics
import android.view.Gravity

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onClickButton()
        //button.setOnClickListener { onClickButton() }
    }


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

    private fun textAsBitmap(textUpper: String, textBottom: String) : Bitmap {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        drawTextOnCanvas(canvas, textUpper, Color.rgb(255,200,200), Gravity.TOP)
        drawTextOnCanvas(canvas, textBottom, Color.rgb(200,200,255), Gravity.BOTTOM)
        return image
    }

    private fun onClickButton() {
        val wm = WallpaperManager.getInstance(this.applicationContext)
        val bitmap = textAsBitmap("hello", "你好")
        wm.setBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), true, FLAG_LOCK)
        Toast.makeText(this.applicationContext, "hi", Toast.LENGTH_SHORT).show()
    }
}

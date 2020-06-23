package com.instructure.ebattaglia.jide

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import kotlin.random.Random

object WallpaperSetter {
    val TAG = "WallpaperSetter"

    val CONTENTS = arrayOf(
        Pair("resist", " 抵 "),
        Pair("retreat/but", " 却 "),
        Pair("kang4ju4", "抗拒")
    )

    private fun drawTextOnCanvas(canvas: Canvas, text: String, textColor: Int, gravity: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
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
        Log.e(TAG, "SETTING WALLPAPER!");
        val wm = WallpaperManager.getInstance(context)
        val content = CONTENTS[Random.nextInt(CONTENTS.size)]
        val bitmap = textAsBitmap(context!!, content.first, content.second)
        wm.setBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), true,
            WallpaperManager.FLAG_LOCK
        )
    }

}
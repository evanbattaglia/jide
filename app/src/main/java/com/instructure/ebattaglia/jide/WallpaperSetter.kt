package com.instructure.ebattaglia.jide

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager

object WallpaperSetter {
    val TAG = "WallpaperSetter"

    private fun drawTextOnCanvas(canvas: Canvas, text: String, textColor: Int, gravity: Int, pctHeight: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var testTextSize = 100f
        paint.textSize = testTextSize
        paint.textAlign = Paint.Align.CENTER
        var baseline = -paint.ascent() // ascent() is negative
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val neededWidth = paint.measureText(text) + 0.5f
        val neededHeight = baseline + bounds.bottom
        // Adjust text size to fit width/height
        paint.textSize = testTextSize * Math.min(canvas.width / neededWidth, canvas.height * pctHeight / neededHeight)
        paint.color =  textColor

        if (gravity == Gravity.BOTTOM) {
            paint.getTextBounds(text, 0, text.length, bounds)
            // put bottom truly on the bottom, which depends on actual content
            // (descenders like "y" and "g" make it go lower)
            canvas.drawText(text, canvas.width/2f, canvas.height - bounds.bottom.toFloat(), paint)
        } else {
            // TOP
            canvas.drawText(text,canvas.width/2f, -paint.ascent(), paint)
        }
    }

    // TODO: fit bottom in bottom 1/3 of screen, fir top in top 25% of screen
    private fun textAsBitmap(context: Context, textUpper: String, textBottom: String) : Bitmap {
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        drawTextOnCanvas(canvas, textUpper, Color.rgb(255,200,200), Gravity.TOP, 0.25f)
        drawTextOnCanvas(canvas, textBottom, Color.rgb(200,200,255), Gravity.BOTTOM, 0.4f)
        return image
    }

    fun setWallpaper(context: Context, top: String, bottom: String, lockScreen: Boolean) {
        val wm = WallpaperManager.getInstance(context)

        val bitmap = textAsBitmap(context, top, bottom)
        wm.setBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), true,
            if (lockScreen) WallpaperManager.FLAG_LOCK else WallpaperManager.FLAG_SYSTEM
        )
        Log.i(TAG, "Setting wallpaper with: ${top}, ${bottom}")
    }

    private fun fieldFromCard(cardFields: Map<String, String>, field: String) =
        cardFields.getOrElse(field) {
            // TODO why don't newlines work
            return "can't find field \"$field\navail fields:\n${cardFields.keys.joinToString("\n")}"
        }

    fun setWallpaper(context: Context) {
        val prefs = JideWallpaperPreferences(context)
        var cardFields = Anki.getCardFields(context, prefs.deckId())

        setWallpaper(context,
            fieldFromCard(cardFields, prefs.lockscreenFirstField()),
            fieldFromCard(cardFields, prefs.lockscreenSecondField()),
            true)
        Log.e("the card is", cardFields["Hanzi"])
        if (!prefs.lockscreenLauncherSame()) {
            cardFields = Anki.getCardFields(context, prefs.deckId())
        }
        Log.e("the card is2", cardFields["Hanzi"])
        setWallpaper(context,
            fieldFromCard(cardFields, prefs.launcherFirstField()),
            fieldFromCard(cardFields, prefs.launcherSecondField()),
            false)
    }

}
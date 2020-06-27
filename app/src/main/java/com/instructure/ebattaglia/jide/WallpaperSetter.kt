package com.instructure.ebattaglia.jide

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.text.HtmlCompat

object WallpaperSetter {
    val TAG = "WallpaperSetter"

    // this handles multiple lines by breaking a string into each line and painting separately, using equal height
    private fun drawTextOnCanvas(canvas: Canvas, textOrHtml: String, textColor: Int, gravity: Int, pctHeight: Float) {
        val text = Html.fromHtml(textOrHtml, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        val texts = text.split("\n").map { it.trim() }.filter { it.length > 0 }
        Log.e("drawTextOnCanvas", "text \"$text\" has ${texts.size} parts")

        val heightOfEach = pctHeight / texts.size
        for (i in texts.indices) {
            val pctUpOrDown = i * heightOfEach
            Log.e("drawTextOnCanvas", "drawing \"${texts[i]}\" at $pctUpOrDown")
            drawTextOnCanvas(canvas, texts[i].trim(), textColor, gravity, heightOfEach, pctUpOrDown)
        }
    }

    private fun drawTextOnCanvas(canvas: Canvas, text: String, textColor: Int, gravity: Int, pctHeight: Float, pctUpOrDown: Float) {
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
            val y = canvas.height - bounds.bottom.toFloat() - pctUpOrDown * canvas.height
            canvas.drawText(text, canvas.width/2f, y, paint)
        } else {
            // TOP
            val y = -paint.ascent() + pctUpOrDown * canvas.height
            canvas.drawText(text,canvas.width/2f, y, paint)
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

    fun setWallpaper(context: Context, note: Anki.Note, fieldNames: Anki.StringPair, useNoteFields: Boolean, lockScreen: Boolean) {
        Log.e(TAG, "$lockScreen using useNoteFields=$useNoteFields")
        val texts =
            if (useNoteFields) {
                Anki.getFieldsFromNote(context, note, fieldNames)
            } else {
                Anki.getCardTemplatesFromNote(context, note, fieldNames)
            }
        val (top, bottom) = texts
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
        var note = Anki.getRandomNote(prefs.deckId())

        val lockscreenFields = Anki.StringPair(prefs.lockscreenFirstField(), prefs.lockscreenSecondField())
        setWallpaper(context, note, lockscreenFields, prefs.lockscreenUseNoteFields(), true)
        if (!prefs.lockscreenLauncherSame()) {
            note = Anki.getRandomNote(prefs.deckId())
        }
        val launcherFields = Anki.StringPair(prefs.launcherFirstField(), prefs.launcherSecondField())
        setWallpaper(context, note, launcherFields, prefs.launcherUseNoteFields(), false)
    }

}
package com.instructure.ebattaglia.jide

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.util.Log
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlin.math.min

object Anki {
    val ANKI_PATH = "/AnkiDroid/collection.anki2"

    fun getCardFields(context: Context, deckId: Long) : Map<String, String> {
        // TODO try SD card as well
        // TODO better error handling above if can't find anki
        // TODO if doing multiple calls, don't open db multiple times
        val path = Environment.getExternalStorageDirectory().absolutePath + ANKI_PATH
        val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)

        Log.e("Anki", "get from deck id = $deckId")
        val query = "select flds, mid from notes where id=(select nid from cards where did=${deckId} and queue >= 0 and queue <= 3 order by random() limit 1)"
        var cursor = db.rawQuery(query, null)
        if (cursor.count < 1) {
            cursor.close()
            val query = "select flds, mid from notes where id=(select nid from cards where did=${deckId} order by random() limit 1)"
            cursor = db.rawQuery(query, null)
        }
        // TODO: if cursor.count is still 0, throw error, and catch it on the other side showing a blank card
        cursor.moveToFirst()
        val flds = cursor.getString(cursor.getColumnIndex("flds")).split(31.toChar())
        val mid = cursor.getLong(cursor.getColumnIndex("mid"))
        cursor.close()
        db.close()

        val fieldNames = AnkiApi.getModelFieldNames(context, mid)
        val map = mutableMapOf<String, String>()
        for (i in 0 until min(fieldNames.size, flds.size)) {
            map[fieldNames[i]] = flds[i]
        }
        return map
    }

    private fun getDBCallback(): SupportSQLiteOpenHelper.Callback {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
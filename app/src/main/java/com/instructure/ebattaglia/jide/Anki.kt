package com.instructure.ebattaglia.jide

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.util.Log
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlin.math.min

object Anki {
    val ANKI_PATH = "/AnkiDroid/collection.anki2"

    data class Note(val id : Long, val flds : String, val mid : Long)

    // TODO should probably move this all in with AnkiApi ...

    // This uses the DB directory so requires external storage READ permission
    fun getRandomNote(deckId: Long) : Note {
        // TODO try SD card as well
        // TODO better error handling above if can't find anki
        // TODO if doing multiple calls, don't open db multiple times
        val path = Environment.getExternalStorageDirectory().absolutePath + ANKI_PATH
        val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)

        Log.e("Anki", "get from deck id = $deckId")
        val query =
            "select id, flds, mid from notes where id=(select nid from cards where did=${deckId} and queue >= 0 and queue <= 3 order by random() limit 1)"
        var cursor = db.rawQuery(query, null)
        if (cursor.count < 1) {
            cursor.close()
            val query =
                "select id, flds, mid from notes where id=(select nid from cards where did=${deckId} order by random() limit 1)"
            cursor = db.rawQuery(query, null)
        }
        // TODO: if cursor.count is still 0, throw error, and catch it on the other side showing a blank card
        cursor.moveToFirst()
        val note = Note(
            id = cursor.getLong(cursor.getColumnIndex("id")),
            flds = cursor.getString(cursor.getColumnIndex("flds")),
            mid = cursor.getLong(cursor.getColumnIndex("mid"))
        )
        cursor.close()
        db.close()
        return note
    }

    data class StringPair(val top : String, val bottom : String)

    fun getFieldsFromNote(context: Context, note : Note, fieldNames: StringPair) : StringPair {
        val allFieldNames = AnkiApi.getModelFieldNames(context, note.mid)
        val map = mutableMapOf<String, String>()
        val flds = note.flds.split(AnkiApi.SEPARATOR_CHAR)
        for (i in 0 until min(allFieldNames.size, flds.size)) {
            map[allFieldNames[i]] = flds[i]
        }
        val top = map.getOrElse(fieldNames.top) {
            "Unknown field name ${fieldNames.top}. Available: ${allFieldNames.joinToString(", ")}"
        }
        val bottom = map.getOrElse(fieldNames.bottom) {
            "Unknown field name ${fieldNames.bottom}. Available: ${allFieldNames.joinToString(", ")}"
        }
        return StringPair(top, bottom)
    }

    fun getCardTemplatesFromNote(context: Context, note: Note, templateNames: StringPair) : StringPair {
        val cr = context.contentResolver
        val templates = AnkiApi.getCardTemplateNameMapFromModelId(cr, note.mid)
        return StringPair(
            top=AnkiApi.getCardTemplateFrontBack(cr, note.id, templates, templateNames.top).first!!,
            bottom=AnkiApi.getCardTemplateFrontBack(cr, note.id, templates, templateNames.bottom).second!!
        )
    }
}
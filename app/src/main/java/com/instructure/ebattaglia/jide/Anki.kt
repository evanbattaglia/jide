package com.instructure.ebattaglia.jide

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import androidx.sqlite.db.SupportSQLiteOpenHelper

object Anki {
    val ANKI_PATH = "/AnkiDroid/collection.anki2"

    fun getCard() : Pair<String, String> {
        val path = Environment.getExternalStorageDirectory().absolutePath + ANKI_PATH
        val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
        val deckId = 1521405849755

        val query = "select flds from notes where id=(select nid from cards where did=${deckId} and queue >= 0 and queue <= 3 order by random() limit 1)"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val flds = cursor.getString(0).split(31.toChar())
        cursor.close()
        db.close()
        return Pair(flds[4], flds[0])

        /*
        val openhelper = SQLiteOpenHelper(context, name, )
        val configuration =
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(ankiFilename)
                .callback(getDBCallback())
                .build()
        val helper = getSqliteOpenHelperFactory().create(configuration)
        mDatabase = helper.getWritableDatabase()
        mDatabase.disableWriteAheadLogging()
        mDatabase.query("PRAGMA synchronous = 2", null)
        mMod = false
         */
    }

    private fun getDBCallback(): SupportSQLiteOpenHelper.Callback {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
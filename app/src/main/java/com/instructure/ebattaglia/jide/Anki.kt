package com.instructure.ebattaglia.jide

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.sqlite.db.SupportSQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream


object Anki {
    fun getCard(application: Application, uri: Uri) : Array<String> {
        val inputStream = application.contentResolver.openInputStream(uri)

        val file = File.createTempFile("sqlite", "")

        val outputStream = FileOutputStream(file)
        val buff = ByteArray(1024)
        var read: Int
        while (true) {
            read = inputStream!!.read(buff, 0, buff.size)
            if (read <= 0) {
                break
            }
            outputStream.write(buff, 0, read)
        }
        inputStream.close()
        outputStream.close()

        return getCard(file.path)
    }

    fun getCard(path: String) : Array<String> {
        val db = SQLiteDatabase.openDatabase(path, null, 0)
        val deckId = 1521405849755

        val query = "select flds from notes where id=(select nid from cards where did=${deckId} order by random() limit 1)"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val flds = cursor.getString(0).split(31.toChar())
        cursor.close()
        return arrayOf(flds[4], flds[0])

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
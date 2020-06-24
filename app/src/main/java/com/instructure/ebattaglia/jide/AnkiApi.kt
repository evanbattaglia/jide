package com.instructure.ebattaglia.jide

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.ichi2.anki.FlashCardsContract

/**
 * Functions to use Anki via the ContentProvider "API"
 */
object AnkiApi {
    private const val TAG = "AnkiApi"
    private const val SEPARATOR_CHAR = 31.toChar()

    fun getDueCardField(context: Context, fieldName: String) : String {
        val cr = context.contentResolver
        var cursor: Cursor

        // TODO simplify, this is very verbose...
        cursor = cr.query(FlashCardsContract.Deck.CONTENT_SELECTED_URI, null, null, null, null)
        cursor.moveToFirst()
        val deckId = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID))
        cursor.close()
        // TODO deck not found

        cursor = cr.query(FlashCardsContract.ReviewInfo.CONTENT_URI, null, "limit=?,deckId=?", arrayOf("1", deckId.toString()), null)
        cursor.moveToFirst()
        val noteId = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.ReviewInfo.NOTE_ID))
        cursor.close()
        // TODO note not found

        cursor = cr.query(
            Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, noteId.toString()),
            arrayOf(FlashCardsContract.Note.FLDS, FlashCardsContract.Note.MID), null, null, null)
        cursor.moveToFirst()
        val fields = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Note.FLDS)).split(
            SEPARATOR_CHAR)
        val modelId = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.Note.MID))
        cursor.close()
        // TODO model ID not found

        cursor = cr.query(
            Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, modelId.toString()),
            arrayOf(FlashCardsContract.Model.FIELD_NAMES), null, null, null
        )
        cursor.moveToFirst()
        val fieldNames = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Model.FIELD_NAMES)).split(
            SEPARATOR_CHAR)
        cursor.close()

        val fieldIndex = fieldNames.indexOf(fieldName)
        if (fieldIndex == -1) {
            return "<field not found, available fields: ${fieldNames.joinToString(", ")}>"
        } else {
            return fields[fieldIndex]
        }
    }
}
package com.instructure.ebattaglia.jide

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.ichi2.anki.FlashCardsContract

/**
 * Functions to use Anki via the ContentProvider "API"
 */
object AnkiApi {
    private const val TAG = "AnkiApi"
    private const val SEPARATOR_CHAR = 31.toChar()

    const val EASE_1 = 1
    const val EASE_2 = 2

    data class Deck(val name: String, val id: Long) {
        override fun toString() = name
    }

    fun getAllDecks(context: Context) : List<Deck> {
        val cr = context.contentResolver
        val cursor = cr.query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null, null)
        val list = (1 .. cursor.count).map {
            cursor.moveToNext()
            val name = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME))
            val id = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID))
            Deck(name, id)
        }
        cursor.close()
        return list
    }

    fun getDueCard(context: Context, deckId: Long, frontFieldName: String, backFieldName: String) : Card {
        Log.d(TAG, "getDueCard deckId=$deckId, frontField=$frontFieldName, backField=$backFieldName")

        val cr = context.contentResolver
        var cursor: Cursor

        // TODO simplify, this is very verbose...
        cursor = cr.query(FlashCardsContract.ReviewInfo.CONTENT_URI, null, "limit=?,deckID=?", arrayOf("1", deckId.toString()), null)
        cursor.moveToFirst()
        val noteId = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.ReviewInfo.NOTE_ID))
        val cardOrd = cursor.getInt(cursor.getColumnIndex(FlashCardsContract.ReviewInfo.CARD_ORD))
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

        var fieldIndex = fieldNames.indexOf(frontFieldName)
        val front =
            if (fieldIndex == -1) {
                // TODO better error handling
                "<field $frontFieldName not found, available fields: ${fieldNames.joinToString(", ")}>"
            } else {
                fields[fieldIndex]
            }

        fieldIndex = fieldNames.indexOf(backFieldName)
        val back =
            if (fieldIndex == -1) {
                "<field $backFieldName not found, available fields: ${fieldNames.joinToString(", ")}>"
            } else {
                fields[fieldIndex]
            }

        return Card(front, back, noteId, cardOrd)
    }

    data class Card(val front: String, val back: String, val noteId: Long, val cardOrd: Int)

    // TODO: pass in note ID and card ord to make sure we're not out of sync
    fun respondCard(context: Context, noteId: Long, cardOrd: Int, ease: Int, timeTaken: Long) {
        val cr = context.contentResolver
        val values = ContentValues()
        values.put(FlashCardsContract.ReviewInfo.NOTE_ID, noteId)
        values.put(FlashCardsContract.ReviewInfo.CARD_ORD, cardOrd)
        values.put(FlashCardsContract.ReviewInfo.EASE, ease)
        values.put(FlashCardsContract.ReviewInfo.TIME_TAKEN, timeTaken)

        Log.i(TAG, "updating $noteId ($cardOrd) with ease $ease")
        cr.update(FlashCardsContract.ReviewInfo.CONTENT_URI, values, null, null)
    }

}
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

    fun getDueCard(context: Context, deckId: Long, useNoteFields: Boolean, frontFieldName: String, backFieldName: String, extraBackFieldName: String) : Card {
        Log.d(
            TAG,
            "getDueCard deckId=$deckId, frontField=$frontFieldName, backField=$backFieldName"
        )

        val cr = context.contentResolver
        val cursor: Cursor
        var front: String? = null
        var back: String? = null
        var extra: String? = null

        // TODO simplify, this is all very verbose...

        cursor = cr.query(
            FlashCardsContract.ReviewInfo.CONTENT_URI,
            null,
            "limit=?,deckID=?",
            arrayOf("1", deckId.toString()),
            null
        )
        if (cursor.count < 1) {
            val noAvailCard = context.getString(R.string.no_available_card)
            cursor.close()
            return Card(noAvailCard, noAvailCard, null, -1, -1)
        }
        cursor.moveToFirst()
        val noteId = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.ReviewInfo.NOTE_ID))
        val cardOrd = cursor.getInt(cursor.getColumnIndex(FlashCardsContract.ReviewInfo.CARD_ORD))
        cursor.close()

        val useCardForFront = frontFieldName.isNullOrBlank()
        val useCardForBack = backFieldName.isNullOrBlank()
        if (useCardForFront || useCardForBack) {
            val (defaultTemplateFront, defaultTemplateBack) = getCardTemplateFrontBack(cr, noteId, cardOrd)
            if (useCardForFront) {
                front = defaultTemplateFront
            }
            if (useCardForBack) {
                back = defaultTemplateBack
            }
        }

        if (useNoteFields) {
            val (noteFront, noteBack, noteExtra) = getNoteFields(cr, noteId, listOf(frontFieldName, backFieldName, extraBackFieldName))
            front = front ?: noteFront
            back = back ?: noteBack
            extra = noteExtra
        } else {
            val templates = getCardTemplateNameMap(cr, noteId)
            front = front ?: getCardTemplateFrontBack(cr, noteId, templates, frontFieldName).first
            back = back ?: getCardTemplateFrontBack(cr, noteId, templates, backFieldName).second
            if (!extraBackFieldName.isNullOrBlank()) {
                extra = getCardTemplateFrontBack(cr, noteId, templates, extraBackFieldName).second
            }
        }

        return Card(front!!, back!!, extra, noteId, cardOrd)
    }

    private fun getCardTemplateNameMap(cr: ContentResolver, noteId: Long) : Map<String, Int> {
        // Get model ID
        var cursor = cr.query(
            Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, noteId.toString()),
            arrayOf(FlashCardsContract.Note.MID), null, null, null)
        if (cursor.count < 1) {
            cursor.close()
            return mapOf()
        }
        cursor.moveToFirst()
        val modelId = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.Note.MID))
        cursor.close()

        // Get card ord
        val result = mutableMapOf<String, Int>()
        cursor = cr.query(
            Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, "$modelId/templates"),
            null, null, null, null
        )
        cursor.moveToFirst()
        for (i in 0 until cursor.count) {
            val name = cursor.getString(cursor.getColumnIndex(FlashCardsContract.CardTemplate.NAME))
            val cardOrd = cursor.getInt(cursor.getColumnIndex(FlashCardsContract.CardTemplate.ORD))
            result.put(name, cardOrd)
            cursor.moveToNext()
        }
        cursor.close()
        return result
    }

    private fun getCardTemplateFrontBack(cr: ContentResolver, noteId: Long, templates: Map<String, Int>, templateName: String): Pair<String?, String?> {
        if (templates.containsKey(templateName)) {
            return getCardTemplateFrontBack(cr, noteId, templates[templateName]!!)
        } else {
            val error = "[Error: unknown template $templateName. Available: ${templates.keys.joinToString(", ")}"
            return Pair(error, error)
        }
    }

    private fun getCardTemplateFrontBack(cr: ContentResolver, noteId: Long, cardOrd: Int): Pair<String?, String?> {
        val cursor = cr.query(
            Uri.withAppendedPath(
                FlashCardsContract.Note.CONTENT_URI,
                noteId.toString() + "/cards/" + cardOrd
            ),
            arrayOf(
                FlashCardsContract.Card.QUESTION_SIMPLE,
                FlashCardsContract.Card.ANSWER_PURE
            ), null, null, null
        )
        cursor.moveToFirst()
        val front = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Card.QUESTION_SIMPLE))
        val back = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Card.ANSWER_PURE))
        cursor.close()
        return Pair(front, back)
    }

    private fun getNoteFields(cr: ContentResolver, noteId: Long, fieldNames: List<String>) : List<String?> {
        val cursor = cr.query(
            Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, noteId.toString()),
            arrayOf(FlashCardsContract.Note.FLDS, FlashCardsContract.Note.MID), null, null, null)
        if (cursor.count < 1) {
            cursor.close()
            return listOf("[Error: Note not found!]", "[Error: Note not found!]", null)
        }
        cursor.moveToFirst()
        val fields = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Note.FLDS)).split(
            SEPARATOR_CHAR)
        val modelId = cursor.getLong(cursor.getColumnIndex(FlashCardsContract.Note.MID))
        cursor.close()

        val allFieldNames = getModelFieldNames(cr, modelId)
        val result = mutableListOf<String?>()
        for (fieldName in fieldNames) {
            var fieldValue : String? = null
            if (!fieldName.isNullOrBlank()) {
                val fieldIndex = allFieldNames.indexOf(fieldName)
                if (fieldIndex == -1) {
                    fieldValue = "<field $fieldName not found, available fields: ${allFieldNames.joinToString(", ")}>"
                } else {
                    fieldValue = fields[fieldIndex]
                }
            }
            result.add(fieldValue)
        }
        return result
    }

    data class Card(val front: String, val back: String, val extra: String?, val noteId: Long, val cardOrd: Int)

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

    fun getModelFieldNames(context: Context, modelId: Long) = getModelFieldNames(context.contentResolver, modelId)

    fun getModelFieldNames(cr: ContentResolver, modelId: Long) : List<String> {
        val cursor = cr.query(
            Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, modelId.toString()),
            arrayOf(FlashCardsContract.Model.FIELD_NAMES), null, null, null
        )
        if (cursor.count == 0) {
            cursor.close()
            return listOf()
        }
        cursor.moveToFirst()
        val fieldNames = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Model.FIELD_NAMES)).split(
            SEPARATOR_CHAR)
        cursor.close()
        return fieldNames
    }

}
package com.instructure.ebattaglia.jide

import android.content.Context
import android.util.Log

class JideWidgetPreferences(context: Context, appWidgetId: Int) {
    companion object {
        private const val TAG = "JideWidgetPreferences"

        private const val SHARED_PREFERENCES_NAMESPACE = "widget_"

        private const val DECK_ID = "deck_id"
        private const val FRONT_FIELD = "front_field"
        private const val BACK_FIELD = "back_field"

        // current card fields
        private const val BACK_TEXT = "back_text"
        private const val NOTE_ID = "note_id"
        private const val CARD_ORD = "card_ord"

        private const val STRIP_HTML_FORMATTING = "strip_html_formatting"


        fun delete(context: Context, appWidgetId: Int) {
            context.deleteSharedPreferences(sharedPrefsName(appWidgetId))
        }

        fun sharedPrefsName(appWidgetId: Int) = SHARED_PREFERENCES_NAMESPACE + appWidgetId
    }
    val prefs = context.getSharedPreferences(sharedPrefsName(appWidgetId), 0)

    fun setConfiguration(
        deckId: Long,
        frontField: String,
        backField: String,
        stripHtmlFormatting: Boolean
    ) {
        Log.d(TAG, "setConfiguration: $deckId, $frontField, $backField, $stripHtmlFormatting")
        val edit = prefs.edit()
        edit.putLong(DECK_ID, deckId)
        edit.putString(FRONT_FIELD, frontField)
        edit.putString(BACK_FIELD, backField)
        edit.putBoolean(STRIP_HTML_FORMATTING, stripHtmlFormatting)
        edit.apply()
    }

    fun getDeckId() = prefs.getLong(DECK_ID, -1)

    fun getFrontFieldName() = prefs.getString(FRONT_FIELD, "")
    fun getBackFieldName() = prefs.getString(BACK_FIELD, "")
    fun getStripHtmlFormatting() = prefs.getBoolean(STRIP_HTML_FORMATTING, true)

    fun setCurrentCard(backText: String, noteId: Long, cardOrd: Int) {
        Log.d(TAG, "SetCurrentCard: $backText, $noteId, $cardOrd")
        val edit = prefs.edit()
        edit.putString(BACK_TEXT, backText)
        edit.putLong(NOTE_ID, noteId)
        edit.putInt(CARD_ORD, cardOrd)
        edit.apply()
    }

    fun getCurrentBackText() = prefs.getString(BACK_TEXT, "")
    fun getCurrentNoteId() = prefs.getLong(NOTE_ID, -1)
    fun getCurrentCardOrd() = prefs.getInt(CARD_ORD, -1)
}
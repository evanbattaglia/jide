package com.instructure.ebattaglia.jide

import android.content.Context

class JideWidgetPreferences(val context: Context, appWidgetId: Int) {
    companion object {
        private const val SHARED_PREFERENCES_NAMESPACE = "widget_"
        private const val ANSWER_VISIBLE = "answer_visible"
    }
    val sharedPrefsName = SHARED_PREFERENCES_NAMESPACE + appWidgetId

    fun delete() {
        context.deleteSharedPreferences(sharedPrefsName)
    }

    fun setNoteFieldNames(frontField: String, backField: String) {
        
    }

    fun isAnswerVisible() : Boolean {
        val prefs = context.getSharedPreferences(sharedPrefsName, 0)
        if (!prefs.contains(ANSWER_VISIBLE)) {
            val edit = prefs.edit()
            edit.putBoolean(ANSWER_VISIBLE, false)
            edit.apply()
            return false
        }
        return prefs.getBoolean(ANSWER_VISIBLE, false)
    }

    fun setAnswerVisible(visible: Boolean) {
        val edit = context.getSharedPreferences(sharedPrefsName, 0).edit()
        edit.putBoolean(ANSWER_VISIBLE, visible)
        edit.apply()
    }
}
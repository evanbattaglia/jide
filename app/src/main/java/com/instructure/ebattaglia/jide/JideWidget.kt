package com.instructure.ebattaglia.jide

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.net.Uri
import android.text.Html
import android.view.View
import androidx.core.text.HtmlCompat

// TODO put these somewhere else
private val SHOW_ANSWER = "SHOW_ANSWER"
private val RESPOND_GOOD = "RESPOND_GOOD"
private val RESPOND_BAD = "RESPOND_BAD"
private val BACK_BUTTON = "BACK_BUTTON"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [JideWidgetConfigureActivity]
 */
class JideWidget : AppWidgetProvider() {
    companion object {
        const val TAG = "JideWidget"

        private fun showAnswerOrExtra(context: Context, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.jide_widget)
            val prefs = JideWidgetPreferences(context, appWidgetId)
            var text : String? = null
            if (prefs.getAnswerCurrentlyShown()) {
                text = prefs.getCurrentExtraBackText()
            }
            if (text.isNullOrBlank()) {
                text = prefs.getCurrentBackText()
                prefs.setAnswerCurrentlyShown(true)
            } else {
                // Showing "extra" text
                prefs.setAnswerCurrentlyShown(false)
            }

            setWidgetText(views, prefs.getCurrentBackText(), prefs.getStripHtmlFormatting())
            views.setViewVisibility(R.id.widget_good_button, View.VISIBLE)
            views.setViewVisibility(R.id.widget_bad_button, View.VISIBLE)
            views.setViewVisibility(R.id.widget_back_button, View.VISIBLE)
            // Instruct the widget manager to update the widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun showQuestion(context: Context, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.jide_widget)
            showQuestion(views, context, appWidgetId)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun setWidgetText(views: RemoteViews, html: String, stripHtml: Boolean) {
            val text = Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val textOnly = text.toString()
            // Work around an Android bug that splits lines mid-word when text size is autosized
            val maxLines = textOnly.trim().split("\\s+".toRegex()).size
            views.setInt(R.id.widget_text, "setMaxLines", maxLines)
            if (stripHtml) {
                views.setTextViewText(R.id.widget_text, textOnly)
            } else {
                views.setTextViewText(R.id.widget_text, text)
            }
        }

        private fun showQuestion(views: RemoteViews, context: Context, appWidgetId: Int) {
            val prefs = JideWidgetPreferences(context, appWidgetId)
            val card =
                AnkiApi.getDueCard(context, prefs.getDeckId(), prefs.getUseNoteFields(), prefs.getFrontFieldName(), prefs.getBackFieldName(), prefs.getExtraBackFieldName())
            prefs.setCurrentCard(card.back, card.extra, card.noteId, card.cardOrd)
            setWidgetText(views, card.front, prefs.getStripHtmlFormatting())

            views.setViewVisibility(R.id.widget_good_button, View.GONE)
            views.setViewVisibility(R.id.widget_bad_button, View.GONE)
            views.setViewVisibility(R.id.widget_back_button, View.GONE)
        }

        private fun respondCard(context: Context, appWidgetId: Int, ease: Int) {
            val prefs = JideWidgetPreferences(context, appWidgetId)
            AnkiApi.respondCard(context, prefs.getCurrentNoteId(), prefs.getCurrentCardOrd(), ease, 10000) // TODO timeTaken
            showQuestion(context, appWidgetId)
        }

        private fun getAppWidgetId(intent: Intent) : Int? {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId == -1) {
                Log.e(TAG, "no appWidgetId provided")
                return null
            }
            return appWidgetId
        }

        private fun setupClickHandler(context: Context, views: RemoteViews, appWidgetId: Int, action: String, widgetResourceId: Int) {
            var intent : Intent = Intent(context, JideWidget::class.java)
            intent.action = action
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // Need to set data to make the intents different so that the same intent is not just updated,
            // the appWidgetId extra data is not just overwritten
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(widgetResourceId, pendingIntent)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.jide_widget)
            showQuestion(views, context, appWidgetId)
            setupClickHandler(context, views, appWidgetId, SHOW_ANSWER, R.id.widget_text)
            setupClickHandler(context, views, appWidgetId, RESPOND_BAD, R.id.widget_bad_button)
            setupClickHandler(context, views, appWidgetId, RESPOND_GOOD, R.id.widget_good_button)
            setupClickHandler(context, views, appWidgetId, BACK_BUTTON, R.id.widget_back_button)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            JideWidgetPreferences.delete(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SHOW_ANSWER -> getAppWidgetId(intent)?.let { showAnswerOrExtra(context, it) }
            RESPOND_GOOD -> getAppWidgetId(intent)?.let { respondCard(context, it, AnkiApi.EASE_2) }
            RESPOND_BAD -> getAppWidgetId(intent)?.let { respondCard(context, it, AnkiApi.EASE_1) }
            BACK_BUTTON -> getAppWidgetId(intent)?.let { showQuestion(context, it) }
        }
        super.onReceive(context, intent)
    }
}


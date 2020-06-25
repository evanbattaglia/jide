package com.instructure.ebattaglia.jide

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.net.Uri
import android.view.View

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

        private fun showAnswer(context: Context, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.jide_widget)
            val prefs = JideWidgetPreferences(context, appWidgetId)
            views.setTextViewText(R.id.widget_text, prefs.getCurrentBackText())
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

        private fun showQuestion(views: RemoteViews, context: Context, appWidgetId: Int) {
            val prefs = JideWidgetPreferences(context, appWidgetId)
            val frontFieldName = prefs.getFrontFieldName()
            if ("".equals(frontFieldName)) {
                views.setTextViewText(R.id.widget_text, "not configured")
            } else {
                Log.d(
                    TAG,
                    "AnkiApi.getDueCard(context, ${prefs.getFrontFieldName()}, ${prefs.getBackFieldName()})"
                )
                val card =
                    AnkiApi.getDueCard(context, prefs.getFrontFieldName(), prefs.getBackFieldName())
                prefs.setCurrentCard(card.back, card.noteId, card.cardOrd)
                views.setTextViewText(R.id.widget_text, card.front)
                views.setViewVisibility(R.id.widget_good_button, View.GONE)
                views.setViewVisibility(R.id.widget_bad_button, View.GONE)
                views.setViewVisibility(R.id.widget_back_button, View.GONE)
            }
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
            SHOW_ANSWER -> getAppWidgetId(intent)?.let { showAnswer(context, it) }
            RESPOND_GOOD -> getAppWidgetId(intent)?.let { respondCard(context, it, AnkiApi.EASE_2) }
            RESPOND_BAD -> getAppWidgetId(intent)?.let { respondCard(context, it, AnkiApi.EASE_1) }
            BACK_BUTTON -> getAppWidgetId(intent)?.let { showQuestion(context, it) }
        }
        super.onReceive(context, intent)
    }
}


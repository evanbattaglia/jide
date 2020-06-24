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

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [JideWidgetConfigureActivity]
 */
class JideWidget : AppWidgetProvider() {
    companion object {
        const val TAG = "JideWidget"
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
            JideWidgetPreferences(context, appWidgetId).delete()
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun showAnswer(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        if (appWidgetId == -1) {
            Log.e(TAG, "no appWidgetId provided");
            return
        }
        val views = RemoteViews(context.packageName, R.layout.jide_widget)
        // TODO: check that state is correct
        // TODO: check that note ID has not changed (pass noteID in extra)
        val widgetText = AnkiApi.getDueCardField(context, "Hanzi")
        views.setTextViewText(R.id.widget_text, widgetText)
        views.setViewVisibility(R.id.widget_good_button, View.VISIBLE)
        views.setViewVisibility(R.id.widget_bad_button, View.VISIBLE)
        // TODO: update noteID in extra, somehow, so we can check noteID
        // Instruct the widget manager to update the widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        JideWidgetPreferences(context, appWidgetId).setAnswerVisible(true)
        // TODO maybe update all widgets here in case we have multiple running using same cards/deck...
    }

    private fun respondCard(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        if (appWidgetId == -1) {
            Log.e(TAG, "no appWidgetId provided");
            return
        }
        val ease = if (intent.action == RESPOND_GOOD) AnkiApi.EASE_2 else AnkiApi.EASE_1
        AnkiApi.respondDueCard(context, ease, 10000) // TODO timeTaken

        JideWidgetPreferences(context, appWidgetId).setAnswerVisible(false)

        // TODO state -> UI dedup and make better (ViewModel stuff? probably won't work with widgets...)
        val views = RemoteViews(context.packageName, R.layout.jide_widget)
        val widgetText = AnkiApi.getDueCardField(context, "Keyword")
        views.setTextViewText(R.id.widget_text, widgetText)
        views.setViewVisibility(R.id.widget_good_button, View.GONE)
        views.setViewVisibility(R.id.widget_bad_button, View.GONE)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SHOW_ANSWER -> showAnswer(context, intent)
            RESPOND_GOOD, RESPOND_BAD -> respondCard(context, intent)
        }
        super.onReceive(context, intent)
    }
}

internal fun setupClickHandler(context: Context, views: RemoteViews, appWidgetId: Int, action: String, widgetResourceId: Int) {
    var intent : Intent = Intent(context, JideWidget::class.java)
    intent.action = action
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    // Need to set data to make the intents different so that the same intent is not just updated,
    // the appWidgetId extra data is not just overwritten
    intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    views.setOnClickPendingIntent(widgetResourceId, pendingIntent)
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // TODO field "Keyword" customizable (and much much later, deck) from prefs
    val widgetText = AnkiApi.getDueCardField(context, "Keyword")

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.jide_widget)
    views.setTextViewText(R.id.widget_text, widgetText)

    setupClickHandler(context, views, appWidgetId, SHOW_ANSWER, R.id.widget_text)
    setupClickHandler(context, views, appWidgetId, RESPOND_BAD, R.id.widget_bad_button)
    setupClickHandler(context, views, appWidgetId, RESPOND_GOOD, R.id.widget_good_button)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
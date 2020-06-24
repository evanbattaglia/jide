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
private val REVIEW_GOOD = "REVIEW_GOOD"
private val REVIEW_BAD = "REVIEW_BAD"

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


    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SHOW_ANSWER -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                if (appWidgetId == -1) {
                    Log.e(TAG, "no appWidgetId provided");
                } else {
                    val views = RemoteViews(context.packageName, R.layout.jide_widget)
                    // TODO: check that state is correct
                    // TODO: check that note ID has not changed (pass noteID in extra)
                    val widgetText = AnkiApi.getDueCardField(context, "Hanzi")
                    views.setTextViewText(R.id.widget_text, widgetText)
                    views.setViewVisibility(R.id.widget_good_button, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_bad_button, View.VISIBLE)
                    // TODO: update noteID in extra, somehow
                    // Instruct the widget manager to update the widget
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    JideWidgetPreferences(context, appWidgetId).setAnswerVisible(true)
                }
            }
            REVIEW_GOOD -> {

            }
            REVIEW_BAD -> {

            }
        }

        super.onReceive(context, intent)
    }
}


internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    //val widgetText2 = loadTitlePref(context, appWidgetId) // TODO get field name (and later, deck name) from prefs
    Log.e("you", "updating, you")

    val widgetText = AnkiApi.getDueCardField(context, "Keyword") // TODO Keyword customizable

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.jide_widget)
    views.setTextViewText(R.id.widget_text, widgetText)

    // EVAN ADDED
    val intent = Intent(context, JideWidget::class.java)
    intent.action = SHOW_ANSWER
    Log.e("PUTTING", "putting appwidgetid $appWidgetId")

    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    intent.putExtra("KEY1", "hello world")
    // Need to set data to make the intents different so that the same intent is not just updated,
    // the appWidgetId extra data is not just overwritten
    intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    views.setOnClickPendingIntent(R.id.widget_text, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
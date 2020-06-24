package com.instructure.ebattaglia.jide

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import android.app.PendingIntent
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import android.content.Intent
import android.util.Log
import android.R.attr.keySet




private val MyOnClick = "myOnClickTag"

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
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    override fun onReceive(context: Context, intent: Intent) {
        if (MyOnClick == intent.action) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            Log.e(TAG, "appWidgetId=$appWidgetId");
            if (appWidgetId == -1) {
              Log.e(TAG, "no appWidgetId provided");
            } else {
                val views = RemoteViews(context.packageName, R.layout.jide_widget)
                views.setTextViewText(R.id.appwidget_text, "hey you")
                // Instruct the widget manager to update the widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.e(TAG, "onReceive update, you")
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
    //val widgetText = loadTitlePref(context, appWidgetId) // TODO get field name (and later, deck name) from prefs
    Log.e("you", "updating, you")

    val widgetText = AnkiApi.getDueCardField(context, "Hanzi")

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.jide_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    // EVAN ADDED
    val intent = Intent(context, JideWidget::class.java)
    intent.action = MyOnClick
    Log.e("PUTTING", "putting appwidgetid $appWidgetId")

    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    intent.putExtra("KEY1", "hello world")

    Log.e("you1", "appwidgetid = $appWidgetId!!!")
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    views.setOnClickPendingIntent(R.id.bad_button, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
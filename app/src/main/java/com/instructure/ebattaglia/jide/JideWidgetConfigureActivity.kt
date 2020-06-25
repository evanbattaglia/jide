package com.instructure.ebattaglia.jide

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.jide_widget_configure.*

/**
 * The configuration screen for the [JideWidget] AppWidget.
 */
class JideWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var onClickListener = View.OnClickListener {
        val context = this@JideWidgetConfigureActivity

        // Store prefs
        val prefs = JideWidgetPreferences(context, appWidgetId)
        val frontField = configure_frontfield.text.toString()
        val backField = configure_backfield.text.toString()
        val deck = configure_deck_spinner.selectedItem as AnkiApi.Deck
        prefs.setNoteFieldNames(frontField, backField)
        prefs.setDeckId(deck.id)

        // Have to run this on our own the first time after configuring
        val appWidgetManager = AppWidgetManager.getInstance(context)
        JideWidget.updateAppWidget(context, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.jide_widget_configure)
        findViewById<View>(R.id.add_button).setOnClickListener(onClickListener)

        // Fill in decks spinner
        val decks : List<AnkiApi.Deck> = AnkiApi.getAllDecks(this)
        val spinnerAdapter = ArrayAdapter<AnkiApi.Deck>(this, android.R.layout.simple_spinner_item, decks)
        // TODO: ^ maybe try R.layout.simple_spinner_dropdown_item
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        configure_deck_spinner.adapter = spinnerAdapter
        //    spinner.setOnItemSelectedListener(this); // TODO maybe field names spinners? with optional text override?

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

}

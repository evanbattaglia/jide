package com.instructure.ebattaglia.jide

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.jide_widget_configure.*

/**
 * The configuration screen for the [JideWidget] AppWidget.
 */
class JideWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    companion object {
        const val GET_ANKI_PERMISSIONS = 1
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            GET_ANKI_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finishSetup()
                } else {
                    Toast.makeText(this, "Need Anki access permissions to add widget", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
    private fun finishSetup() {
        add_button.setOnClickListener(onClickListener)

        // Fill in decks spinner
        val decks : List<AnkiApi.Deck> = AnkiApi.getAllDecks(this)
        val spinnerAdapter = ArrayAdapter<AnkiApi.Deck>(this, android.R.layout.simple_spinner_dropdown_item, decks)
        configure_deck_spinner.adapter = spinnerAdapter
        //  spinner.setOnItemSelectedListener(this); // TODO maybe field names spinners? with optional text override?
    }

    private var onClickListener = View.OnClickListener {
        val context = this@JideWidgetConfigureActivity

        // Store prefs
        val prefs = JideWidgetPreferences(context, appWidgetId)
        val useNoteFields = configure_use_note_fields_instead_of_card_templates.isChecked
        val frontField = configure_frontfield.text.toString()
        val backField = configure_backfield.text.toString()
        val extraBackField = configure_extra_backfield.text.toString()
        val stripHtmlFormatting = configure_strip_html_formatting_switch.isChecked
        val deck = configure_deck_spinner.selectedItem as AnkiApi.Deck

        prefs.setConfiguration(deck.id, useNoteFields, frontField, backField, extraBackField, stripHtmlFormatting)

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
        // Need permissions before we can show configuration activity (need to fill in deck names)
        // TODO check permissions and skip if already have permissions
        requestPermissions(
            arrayOf("com.ichi2.anki.permission.READ_WRITE_DATABASE"),
            MainActivity.GET_ANKI_PERMISSIONS
        )

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

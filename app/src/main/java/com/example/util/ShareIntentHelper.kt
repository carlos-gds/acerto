package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Utility for building and launching system share intents (ACTION_SEND with plain text).
 * Separates intent creation (testable in JVM/Robolectric) from context interaction.
 */
object ShareIntentHelper {

    /**
     * Builds the base ACTION_SEND intent with text/plain and EXTRA_TEXT.
     */
    fun createSendIntent(text: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    /**
     * Builds the system chooser Intent wrapping the send intent.
     */
    fun createChooserIntent(
        text: String,
        chooserTitle: String = "Compartilhar cobrança"
    ): Intent {
        val sendIntent = createSendIntent(text)
        return Intent.createChooser(sendIntent, chooserTitle)
    }

    /**
     * Launches the share chooser via Android Context.
     * Handles missing handler apps gracefully without crashing.
     *
     * @return true if launched successfully, false if no app was found or an error occurred.
     */
    fun sharePlainText(
        context: Context,
        text: String,
        chooserTitle: String = "Compartilhar cobrança",
        onNoAppFound: (() -> Unit)? = null
    ): Boolean {
        return try {
            val chooser = createChooserIntent(text, chooserTitle)
            if (context !is android.app.Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: ActivityNotFoundException) {
            if (onNoAppFound != null) {
                onNoAppFound()
            } else {
                Toast.makeText(context, "Nenhum aplicativo compatível encontrado", Toast.LENGTH_SHORT).show()
            }
            false
        } catch (_: Exception) {
            if (onNoAppFound != null) {
                onNoAppFound()
            } else {
                Toast.makeText(context, "Não foi possível compartilhar a cobrança", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}

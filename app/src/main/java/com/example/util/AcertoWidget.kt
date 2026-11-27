package com.example.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.InstallmentItem

/** Widget da tela inicial: "Falta receber R$ X · N pessoas" com atalhos Receber e Compra. */
class AcertoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        manager.updateAppWidget(ids, AcertoWidget.views(context))
    }
}

/** Ações abertas pelos atalhos do ícone e pelo widget. */
object AppActions {
    const val RECEIVE = "com.example.acerto.RECEIVE"
    const val NEW_PURCHASE = "com.example.acerto.NEW_PURCHASE"
    val all = setOf(RECEIVE, NEW_PURCHASE)
}

object AcertoWidget {
    private const val PREFS = "acerto_widget"

    /** Resumo do que falta receber (tudo em aberto, sem a minha parte). */
    data class Snapshot(val pendingCents: Long, val people: Int)

    fun snapshotOf(installments: List<InstallmentItem>): Snapshot {
        val open = installments.filter { !it.isPaid && !it.isMyShare && it.amount > 0 }
        return Snapshot(open.sumOf { it.amount }, open.map { it.personId }.distinct().size)
    }

    /** Guarda o resumo e redesenha os widgets só quando algo mudou. */
    fun save(context: Context, snapshot: Snapshot, hidden: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getLong("cents", -1) == snapshot.pendingCents &&
            prefs.getInt("people", -1) == snapshot.people &&
            prefs.getBoolean("hidden", false) == hidden
        ) return
        prefs.edit()
            .putLong("cents", snapshot.pendingCents)
            .putInt("people", snapshot.people)
            .putBoolean("hidden", hidden)
            .apply()
        refresh(context)
    }

    fun refresh(context: Context) {
        val manager = AppWidgetManager.getInstance(context) ?: return
        val ids = manager.getAppWidgetIds(ComponentName(context, AcertoWidgetProvider::class.java))
        if (ids.isNotEmpty()) manager.updateAppWidget(ids, views(context))
    }

    fun views(context: Context): RemoteViews {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val cents = prefs.getLong("cents", 0L).coerceAtLeast(0L)
        val people = prefs.getInt("people", 0)
        val hidden = prefs.getBoolean("hidden", false)
        return RemoteViews(context.packageName, R.layout.widget_acerto).apply {
            setTextViewText(R.id.widget_amount, CurrencyFormatter.format(Money.toReal(cents), hidden))
            setTextViewText(
                R.id.widget_subtitle,
                when {
                    people == 0 -> "Ninguém te deve"
                    people == 1 -> "de 1 pessoa"
                    else -> "de $people pessoas"
                }
            )
            setOnClickPendingIntent(R.id.widget_root, open(context, null, 0))
            setOnClickPendingIntent(R.id.widget_receive, open(context, AppActions.RECEIVE, 1))
            setOnClickPendingIntent(R.id.widget_purchase, open(context, AppActions.NEW_PURCHASE, 2))
        }
    }

    private fun open(context: Context, action: String?, code: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            this.action = action ?: Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

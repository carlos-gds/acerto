package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.InstallmentItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Lembrete mensal: no dia 1, às 9h, avisa quanto falta receber no mês.
 * Usa o AlarmManager do sistema (alarme inexato, sem permissão especial
 * e sem biblioteca), reagendado a cada disparo e após reiniciar o celular.
 */
object MonthlyReminder {
    private const val PREFS = "acerto_prefs"
    private const val KEY = "monthly_reminder"
    private const val KEY_DUE = "card_due_day"
    /** Quantos dias antes do vencimento da fatura o lembrete chega. */
    const val DAYS_BEFORE_DUE = 3
    const val CHANNEL_ID = "lembretes"
    const val ACTION = "com.example.acerto.MONTHLY_REMINDER"
    private const val NOTIFICATION_ID = 1001

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY, false)

    /** Dia de vencimento da fatura do cartão (1–31), ou 0 se não informado. */
    fun getDueDay(context: Context): Int = prefs(context).getInt(KEY_DUE, 0)

    fun setDueDay(context: Context, day: Int) {
        prefs(context).edit().putInt(KEY_DUE, day.coerceIn(0, 31)).apply()
        if (isEnabled(context)) schedule(context)
    }

    /** Dia do mês em que o lembrete chega: 3 dias antes do vencimento, ou dia 1. */
    fun reminderDay(dueDay: Int): Int = if (dueDay > 0) (dueDay - DAYS_BEFORE_DUE).coerceAtLeast(1) else 1

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY, enabled).apply()
        if (enabled) schedule(context) else cancel(context)
    }

    /**
     * Próximo lembrete às 9h: no dia 1, ou 3 dias antes do vencimento da fatura
     * quando ele foi informado. Em meses curtos, o dia é ajustado ao último dia.
     */
    fun nextTrigger(now: Calendar = Calendar.getInstance(), dueDay: Int = 0): Calendar {
        fun at(monthOffset: Int): Calendar {
            val c = now.clone() as Calendar
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.add(Calendar.MONTH, monthOffset)
            val day = reminderDay(dueDay).coerceAtMost(c.getActualMaximum(Calendar.DAY_OF_MONTH))
            c.set(Calendar.DAY_OF_MONTH, day)
            c.set(Calendar.HOUR_OF_DAY, 9)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            return c
        }
        val thisMonth = at(0)
        return if (thisMonth.after(now)) thisMonth else at(1)
    }

    private fun alarmIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 0,
            Intent(context, MonthlyReminderReceiver::class.java).setAction(ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun schedule(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        am.set(AlarmManager.RTC_WAKEUP, nextTrigger(dueDay = getDueDay(context)).timeInMillis, alarmIntent(context))
    }

    fun cancel(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(alarmIntent(context))
    }

    /** Texto do lembrete a partir das parcelas do mês (só de outras pessoas, não pagas). */
    fun message(items: List<InstallmentItem>, year: Int, month: Int, dueDay: Int = 0): Pair<String, String>? {
        val pending = items.filter { it.year == year && it.month == month && !it.isMyShare && !it.isPaid }
        if (pending.isEmpty()) return null
        val people = pending.map { it.personId }.distinct().size
        val total = Money.toReal(pending.sumOf { it.amount })
        val monthName = CurrencyFormatter.getMonthName(month).lowercase()
        val who = if (people == 1) "1 pessoa" else "$people pessoas"
        return if (dueDay > 0) {
            "Sua fatura vence dia $dueDay" to
                "Ainda faltam ${CurrencyFormatter.format(total)} de $who. Cobre antes do vencimento."
        } else {
            val title = if (people == 1) "1 pessoa te deve em $monthName" else "$people pessoas te devem em $monthName"
            title to "Falta receber ${CurrencyFormatter.format(total)}. Toque para ver e cobrar."
        }
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Lembretes de cobrança", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Aviso no início do mês com quanto falta receber"
                }
            )
        }
    }

    /** Lê o banco e mostra o lembrete do mês atual. [force] mostra um exemplo mesmo sem pendências. */
    suspend fun showForCurrentMonth(context: Context, force: Boolean = false) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val items = AppDatabase.getDatabase(context).installmentDao().getAllInstallmentsList()
        val msg = message(items, year, month, getDueDay(context))
            ?: if (force) "Nada a receber em ${CurrencyFormatter.getMonthName(month).lowercase()}" to "Ninguém te deve neste mês. Este é só um exemplo do lembrete." else return
        notify(context, msg.first, msg.second)
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun notify(context: Context, title: String, body: String) {
        if (!canNotify(context)) return
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, n)
    }
}

/** Recebe o alarme do dia 1 e o reinício do celular (para reagendar). */
class MonthlyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!MonthlyReminder.isEnabled(context)) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == MonthlyReminder.ACTION) {
                    MonthlyReminder.showForCurrentMonth(context)
                }
                MonthlyReminder.schedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}

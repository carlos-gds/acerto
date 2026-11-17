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
    const val CHANNEL_ID = "lembretes"
    const val ACTION = "com.example.acerto.MONTHLY_REMINDER"
    private const val NOTIFICATION_ID = 1001

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY, enabled).apply()
        if (enabled) schedule(context) else cancel(context)
    }

    /** Próximo dia 1 às 9h (se hoje é dia 1 antes das 9h, é hoje). */
    fun nextTrigger(now: Calendar = Calendar.getInstance()): Calendar {
        val c = now.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 9)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        if (!c.after(now)) c.add(Calendar.MONTH, 1)
        return c
    }

    private fun alarmIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 0,
            Intent(context, MonthlyReminderReceiver::class.java).setAction(ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun schedule(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        am.set(AlarmManager.RTC_WAKEUP, nextTrigger().timeInMillis, alarmIntent(context))
    }

    fun cancel(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(alarmIntent(context))
    }

    /** Texto do lembrete a partir das parcelas do mês (só de outras pessoas, não pagas). */
    fun message(items: List<InstallmentItem>, year: Int, month: Int): Pair<String, String>? {
        val pending = items.filter { it.year == year && it.month == month && !it.isMyShare && !it.isPaid }
        if (pending.isEmpty()) return null
        val people = pending.map { it.personId }.distinct().size
        val total = Money.toReal(pending.sumOf { it.amount })
        val monthName = CurrencyFormatter.getMonthName(month).lowercase()
        val title = if (people == 1) "1 pessoa te deve em $monthName" else "$people pessoas te devem em $monthName"
        val body = "Falta receber ${CurrencyFormatter.format(total)}. Toque para ver e cobrar."
        return title to body
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
        val msg = message(items, year, month)
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

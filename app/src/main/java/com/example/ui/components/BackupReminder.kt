package com.example.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLiquidGlassColors

/**
 * Lembrete de backup: os dados só existem no celular, então o app avisa quando
 * faz tempo que não se salva uma cópia (ou quando nunca se salvou), com um toque
 * para salvar no Drive ou onde a pessoa quiser.
 */
object BackupReminder {
    private const val PREFS = "acerto_prefs"
    private const val LAST = "backup_last_at"
    private const val SNOOZE = "backup_snooze_until"
    private const val DATA_SINCE = "backup_data_since"
    private const val DAY = 24L * 60 * 60 * 1000

    /** Com backup feito: lembra depois de tantos dias sem um novo. */
    const val EVERY_DAYS = 30
    /** Sem nenhum backup: lembra tantos dias depois do primeiro lançamento. */
    const val FIRST_AFTER_DAYS = 3
    /** "Depois": volta a lembrar em tantos dias. */
    const val SNOOZE_DAYS = 7

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun lastBackupAt(context: Context) = prefs(context).getLong(LAST, 0L)
    fun snoozeUntil(context: Context) = prefs(context).getLong(SNOOZE, 0L)
    fun dataSince(context: Context) = prefs(context).getLong(DATA_SINCE, 0L)

    /** Backup salvo agora: zera o lembrete. */
    fun markBackedUp(context: Context, now: Long = System.currentTimeMillis()) {
        prefs(context).edit().putLong(LAST, now).remove(SNOOZE).apply()
    }

    fun snooze(context: Context, now: Long = System.currentTimeMillis()) {
        prefs(context).edit().putLong(SNOOZE, now + SNOOZE_DAYS * DAY).apply()
    }

    /** Primeira vez que o app tem dados (conta o prazo do primeiro backup a partir daí). */
    fun noteHasData(context: Context, now: Long = System.currentTimeMillis()) {
        if (dataSince(context) == 0L) prefs(context).edit().putLong(DATA_SINCE, now).apply()
    }

    /** Regra do lembrete (pura, para testar). */
    fun shouldRemind(now: Long, hasData: Boolean, lastAt: Long, dataSince: Long, snoozeUntil: Long): Boolean {
        if (!hasData || now < snoozeUntil) return false
        return if (lastAt == 0L) {
            dataSince > 0L && now - dataSince >= FIRST_AFTER_DAYS * DAY
        } else {
            now - lastAt >= EVERY_DAYS * DAY
        }
    }

    fun daysSince(now: Long, at: Long): Int = ((now - at) / DAY).toInt().coerceAtLeast(0)

    /** "hoje", "ontem", "há 5 dias" (ou "nunca"). */
    fun lastBackupLabel(now: Long, lastAt: Long): String = when {
        lastAt == 0L -> "nunca"
        else -> when (val d = daysSince(now, lastAt)) {
            0 -> "hoje"
            1 -> "ontem"
            else -> "há $d dias"
        }
    }

    internal fun isKey(key: String?) = key == LAST || key == SNOOZE || key == DATA_SINCE
}

/** Estado observável do lembrete (atualiza assim que um backup é salvo). */
@Stable
class BackupReminderState internal constructor(private val context: Context) {
    var lastAt by mutableLongStateOf(BackupReminder.lastBackupAt(context))
        private set
    var snoozeUntil by mutableLongStateOf(BackupReminder.snoozeUntil(context))
        private set
    var dataSince by mutableLongStateOf(BackupReminder.dataSince(context))
        private set

    internal fun reload() {
        lastAt = BackupReminder.lastBackupAt(context)
        snoozeUntil = BackupReminder.snoozeUntil(context)
        dataSince = BackupReminder.dataSince(context)
    }

    fun shouldRemind(hasData: Boolean, now: Long = System.currentTimeMillis()) =
        BackupReminder.shouldRemind(now, hasData, lastAt, dataSince, snoozeUntil)

    fun noteHasData() = BackupReminder.noteHasData(context)
    fun snooze() = BackupReminder.snooze(context)
}

@Composable
fun rememberBackupReminderState(): BackupReminderState {
    val context = LocalContext.current
    val state = remember(context) { BackupReminderState(context) }
    DisposableEffect(context) {
        val p = context.applicationContext.getSharedPreferences("acerto_prefs", Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || BackupReminder.isKey(key)) state.reload()
        }
        p.registerOnSharedPreferenceChangeListener(listener)
        onDispose { p.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return state
}

/** Cartão na Início: "Faz 34 dias sem backup" com Fazer backup / Depois. */
@Composable
fun BackupReminderCard(
    lastAt: Long,
    onBackup: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
    now: Long = System.currentTimeMillis()
) {
    val liquid = LocalLiquidGlassColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(liquid.cardSurfaceVariant)
            .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("backup_reminder")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(liquid.pillPrimaryBg),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = liquid.pillPrimaryText, modifier = Modifier.size(22.dp)) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    if (lastAt == 0L) "Você ainda não fez backup" else "Faz ${BackupReminder.daysSince(now, lastAt)} dias sem backup",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = liquid.textPrimary
                )
                Text(
                    "Tudo fica só neste celular. Salve uma cópia no Drive ou onde preferir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = liquid.textMuted
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Text(
                "Depois",
                style = MaterialTheme.typography.labelLarge,
                color = liquid.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(role = Role.Button, onClick = onLater)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("backup_reminder_later")
            )
            Text(
                "Fazer backup",
                style = MaterialTheme.typography.labelLarge,
                color = liquid.pillPrimaryText,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(liquid.pillPrimaryBg)
                    .clickable(role = Role.Button, onClick = onBackup)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .testTag("backup_reminder_now")
            )
        }
    }
}

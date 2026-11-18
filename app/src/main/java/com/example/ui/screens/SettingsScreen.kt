package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.util.PixKeyStore
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.GlassDivider
import com.example.ui.components.glass.liquidGlass
import com.example.ui.components.glass.staggeredAppear
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.glass.rememberAppearTracker
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.components.glass.liquidFieldMotion

/**
 * Aba Ajustes: tema, privacidade, pessoas e backup/segurança.
 * Só dispara os mesmos callbacks que antes ficavam no cabeçalho da Home.
 */
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    isPrivacyMode: Boolean,
    peopleCount: Int,
    onToggleTheme: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onManagePeopleClick: () -> Unit,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 140.dp)
) {
    val liquid = LocalLiquidGlassColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
            Text(
                text = buildAnnotatedString {
                    append("Ajustes")
                    withStyle(SpanStyle(color = liquid.accent)) { append(".") }
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = liquid.textPrimary
            )
            Text(
                text = "Aparência, privacidade e seus dados",
                style = MaterialTheme.typography.bodySmall,
                color = liquid.textSecondary
            )
        }

        SettingsGroup(title = "Aparência e privacidade") {
            SettingsRow(
                icon = Icons.Default.DarkMode,
                title = "Tema escuro",
                subtitle = if (isDarkMode) "Tema escuro ativo" else "Tema claro ativo",
                testTag = "theme_toggle_button",
                onClick = onToggleTheme,
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleTheme() },
                        colors = liquidSwitchColors()
                    )
                }
            )
            GlassDivider(modifier = Modifier.padding(horizontal = 12.dp))
            SettingsRow(
                icon = Icons.Default.VisibilityOff,
                title = "Modo privacidade",
                subtitle = "Oculta os valores nas telas",
                testTag = "settings_privacy_switch",
                onClick = onTogglePrivacy,
                trailing = {
                    Switch(
                        checked = isPrivacyMode,
                        onCheckedChange = { onTogglePrivacy() },
                        colors = liquidSwitchColors()
                    )
                }
            )
        }

        SettingsGroup(title = "Segurança e lembretes") {
            AppLockRow()
            GlassDivider(modifier = Modifier.padding(horizontal = 12.dp))
            ReminderRow()
        }

        SettingsGroup(title = "Pessoas") {
            SettingsRow(
                icon = Icons.Default.Group,
                title = "Gerenciar pessoas",
                subtitle = if (peopleCount == 1) "1 pessoa cadastrada" else "$peopleCount pessoas cadastradas",
                testTag = "manage_people_top_button",
                onClick = onManagePeopleClick
            )
        }

        // Chave PIX: entra automaticamente no texto de cobrança enviado
        SettingsGroup(title = "Cobrança") {
            PixKeySetting()
        }

        SettingsGroup(title = "Dados e segurança") {
            SettingsRow(
                icon = Icons.Default.Security,
                title = "Backup e segurança",
                subtitle = "Exportar ou restaurar seus dados em JSON",
                testTag = "top_bar_backup_button",
                onClick = onBackupClick
            )
        }
    }
}

@Composable
private fun liquidSwitchColors() = LocalLiquidGlassColors.current.let { liquid ->
    SwitchDefaults.colors(
        checkedThumbColor = liquid.pillPrimaryText,
        checkedTrackColor = liquid.pillPrimaryBg,
        checkedBorderColor = liquid.pillPrimaryBg,
        uncheckedThumbColor = liquid.textSecondary,
        uncheckedTrackColor = liquid.cardSurfaceVariant,
        uncheckedBorderColor = liquid.cardBorder
    )
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val tracker = rememberAppearTracker()
    val index = remember(title) { SettingsGroupOrder.indexOf(title).coerceAtLeast(0) }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.staggeredAppear("settings_$title", index, tracker, !rememberReducedMotion())
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = liquid.textPrimary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = LiquidShapes.card, elevation = LocalLiquidElevation.current.medium)
                .padding(vertical = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val liquid = LocalLiquidGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(LiquidShapes.iconTile)
                .background(liquid.accent.copy(alpha = 0.16f))
                .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.iconTile),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = liquid.accent, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = liquid.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = liquid.textMuted)
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = liquid.textSecondary
            )
        }
    }
}

/** Campo da chave PIX (guardada só no aparelho) com tipo detectado e salvar/remover. */
@Composable
private fun PixKeySetting() {
    val liquid = LocalLiquidGlassColors.current
    val context = LocalContext.current
    val savedKey by PixKeyStore.rememberPixKey()
    var draft by remember(savedKey) { mutableStateOf(savedKey) }
    var savedTick by remember { mutableStateOf(0) }
    val savedName = remember(savedKey, savedTick) { PixKeyStore.getName(context) }
    val savedBank = remember(savedKey, savedTick) { PixKeyStore.getBank(context) }
    var nameDraft by remember(savedName) { mutableStateOf(savedName) }
    var bankDraft by remember(savedBank) { mutableStateOf(savedBank) }
    val changed = draft.trim() != savedKey || nameDraft.trim() != savedName || bankDraft.trim() != savedBank
    val keyType = PixKeyStore.describe(draft)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(LiquidShapes.iconTile)
                    .background(liquid.accent.copy(alpha = 0.16f))
                    .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.iconTile),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = null, tint = liquid.accent, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Chave PIX", style = MaterialTheme.typography.titleSmall, color = liquid.textPrimary)
                Text(
                    text = if (savedKey.isBlank()) "Aparece no texto de cobrança enviado"
                    else "Incluída nas cobranças enviadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = liquid.textMuted
                )
            }
        }

        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it.take(77) },
            modifier = Modifier.liquidFieldMotion(draft)
                .fillMaxWidth()
                .testTag("settings_pix_key_input"),
            singleLine = true,
            label = { Text("CPF, CNPJ, e-mail, celular ou chave aleatória") },
            supportingText = if (keyType.isNotEmpty()) {
                { Text("Tipo detectado: $keyType") }
            } else null,
            shape = LiquidShapes.field,
            colors = liquidTextFieldColors()
        )

        OutlinedTextField(
            value = nameDraft,
            onValueChange = { nameDraft = it.take(40) },
            modifier = Modifier.fillMaxWidth().testTag("settings_pix_name_input"),
            singleLine = true,
            label = { Text("Seu nome, como no banco") },
            shape = LiquidShapes.field,
            colors = liquidTextFieldColors()
        )
        OutlinedTextField(
            value = bankDraft,
            onValueChange = { bankDraft = it.take(30) },
            modifier = Modifier.fillMaxWidth().testTag("settings_pix_bank_input"),
            singleLine = true,
            label = { Text("Banco (opcional)") },
            placeholder = { Text("Ex.: Nubank, Itaú, Caixa") },
            supportingText = {
                Text("Seu nome e o banco aparecem na cobrança, junto com a chave e o tipo dela.")
            },
            shape = LiquidShapes.field,
            colors = liquidTextFieldColors()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            if (savedKey.isNotBlank()) {
                TextButton(
                    onClick = {
                        PixKeyStore.set(context, "")
                        draft = ""
                    },
                    modifier = Modifier.testTag("settings_pix_key_remove")
                ) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            }
            Button(
                onClick = {
                    PixKeyStore.setReceiver(context, nameDraft, bankDraft)
                    PixKeyStore.set(context, draft)
                    savedTick++
                },
                enabled = changed,
                shape = LiquidShapes.pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = liquid.pillPrimaryBg,
                    contentColor = liquid.pillPrimaryText
                ),
                modifier = Modifier.testTag("settings_pix_key_save")
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Ordem de entrada dos grupos de Ajustes (para o escalonamento). */
private val SettingsGroupOrder = listOf("Aparência e privacidade", "Segurança e lembretes", "Pessoas", "Cobrança", "Dados e segurança")

/** Bloquear o app com a digital/rosto ou a senha do celular. */
@Composable
private fun AppLockRow() {
    val context = LocalContext.current
    val available = remember(context) { com.example.util.AppLock.isAvailable(context) }
    var enabled by com.example.util.AppLock.rememberEnabled()
    SettingsRow(
        icon = Icons.Default.Lock,
        title = "Bloquear o app",
        subtitle = when {
            !available -> "Configure uma senha no celular para usar"
            enabled -> "Pede digital ou senha ao abrir"
            else -> "Desligado"
        },
        testTag = "settings_app_lock",
        onClick = {
            if (available) {
                enabled = !enabled
                com.example.util.AppLock.setEnabled(context, enabled)
            }
        },
        trailing = {
            Switch(
                checked = enabled && available,
                enabled = available,
                onCheckedChange = {
                    enabled = it
                    com.example.util.AppLock.setEnabled(context, it)
                },
                colors = liquidSwitchColors()
            )
        }
    )
}

/** Lembrete no dia 1 de cada mês, com um botão para ver um exemplo agora. */
@Composable
private fun ReminderRow() {
    val liquid = LocalLiquidGlassColors.current
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var enabled by remember(context) { mutableStateOf(com.example.util.MonthlyReminder.isEnabled(context)) }
    var showExampleAfterGrant by remember { mutableStateOf(false) }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enabled = true
            com.example.util.MonthlyReminder.setEnabled(context, true)
            if (showExampleAfterGrant) scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                com.example.util.MonthlyReminder.showForCurrentMonth(context, force = true)
            }
        }
        showExampleAfterGrant = false
    }
    fun turn(on: Boolean) {
        if (on && !com.example.util.MonthlyReminder.canNotify(context)) {
            permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
        } else {
            enabled = on
            com.example.util.MonthlyReminder.setEnabled(context, on)
        }
    }
    var dueDay by remember(context) { mutableStateOf(com.example.util.MonthlyReminder.getDueDay(context)) }
    var editingDue by remember { mutableStateOf(false) }
    if (editingDue) {
        DueDayDialog(
            current = dueDay,
            onDismiss = { editingDue = false },
            onSave = { day ->
                dueDay = day
                com.example.util.MonthlyReminder.setDueDay(context, day)
                editingDue = false
            }
        )
    }
    Column {
        SettingsRow(
            icon = Icons.Default.Notifications,
            title = "Lembrete mensal",
            subtitle = when {
                !enabled -> "Desligado"
                dueDay > 0 -> "Dia ${com.example.util.MonthlyReminder.reminderDay(dueDay)} às 9h, 3 dias antes da fatura"
                else -> "Dia 1 às 9h: quanto falta receber"
            },
            testTag = "settings_monthly_reminder",
            onClick = { turn(!enabled) },
            trailing = {
                Switch(checked = enabled, onCheckedChange = { turn(it) }, colors = liquidSwitchColors())
            }
        )
        GlassDivider(modifier = Modifier.padding(horizontal = 12.dp))
        SettingsRow(
            icon = Icons.Default.CreditCard,
            title = "Vencimento da fatura",
            subtitle = if (dueDay > 0) "Dia $dueDay de cada mês" else "Não informado",
            testTag = "settings_due_day",
            onClick = { editingDue = true }
        )
        if (enabled) {
            TextButton(
                onClick = {
                    if (!com.example.util.MonthlyReminder.canNotify(context)) {
                        showExampleAfterGrant = true
                        permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                    } else {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            com.example.util.MonthlyReminder.showForCurrentMonth(context, force = true)
                        }
                    }
                },
                modifier = Modifier.padding(start = 60.dp, bottom = 4.dp).testTag("settings_reminder_preview")
            ) {
                Text("Ver como fica agora", color = liquid.textPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Escolha do dia de vencimento da fatura (1 a 31), ou remover. */
@Composable
private fun DueDayDialog(current: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    val liquid = LocalLiquidGlassColors.current
    var text by remember { mutableStateOf(if (current > 0) current.toString() else "") }
    val day = text.toIntOrNull()
    val valid = day != null && day in 1..31
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = liquid.sheetSurface,
        title = { Text("Vencimento da fatura", color = liquid.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Com o dia do vencimento, o lembrete chega 3 dias antes, para dar tempo de cobrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { v -> text = v.filter { it.isDigit() }.take(2) },
                    singleLine = true,
                    label = { Text("Dia (1 a 31)") },
                    isError = text.isNotEmpty() && !valid,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = LiquidShapes.field,
                    colors = liquidTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("due_day_input")
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { day?.let(onSave) }, enabled = valid) { Text("Salvar", color = liquid.textPrimary) }
        },
        dismissButton = {
            Row {
                if (current > 0) TextButton(onClick = { onSave(0) }) { Text("Remover", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = liquid.textSecondary) }
            }
        }
    )
}

package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.GlassIconButton
import com.example.ui.theme.LocalLiquidGlassColors

/**
 * Top bar for the Acerto home screen, including people management, title,
 * quick AI shortcut, theme switcher, and tab selector.
 *
 * Estilo Liquid Glass (Figma): sem barra sólida, o fundo obsidian aparece por trás;
 * título à esquerda com o ponto no acento e botões circulares de vidro à direita.
 */
@Composable
fun HomeTopBar(
    selectedTabIndex: Int,
    isDarkMode: Boolean,
    onManagePeopleClick: () -> Unit,
    onAiQuickClick: () -> Unit,
    onToggleThemeClick: () -> Unit,
    onBackupClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        append("Acerto")
                        withStyle(SpanStyle(color = liquid.accent)) { append(".") }
                    },
                    // 18sp em vez dos 22 do Figma: cabe ao lado dos 4 botões em telas de 360 dp.
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = liquid.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (selectedTabIndex == 0) "Fatura Mensal" else "Parcelas no Cartão",
                    style = MaterialTheme.typography.bodySmall,
                    color = liquid.textSecondary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Entrada rápida com IA (vidro com gradiente do acento)
                GlassIconButton(
                    icon = Icons.Default.AutoAwesome,
                    contentDescription = "Entrada Rápida IA",
                    onClick = onAiQuickClick,
                    accent = true,
                    size = 42.dp,
                    modifier = Modifier.testTag("top_bar_ai_button")
                )

                GlassIconButton(
                    icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkMode) "Alternar para Modo Claro" else "Alternar para Modo Escuro",
                    onClick = onToggleThemeClick,
                    size = 42.dp,
                    modifier = Modifier.testTag("theme_toggle_button")
                )

                GlassIconButton(
                    icon = Icons.Default.Security,
                    contentDescription = "Backup e Segurança",
                    onClick = onBackupClick,
                    size = 42.dp,
                    modifier = Modifier.testTag("top_bar_backup_button")
                )

                GlassIconButton(
                    icon = Icons.Default.Group,
                    contentDescription = "Pessoas e Divisões",
                    onClick = onManagePeopleClick,
                    size = 42.dp,
                    modifier = Modifier.testTag("manage_people_top_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Seletor de abas em cápsula de vidro
        HomeTabSelector(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

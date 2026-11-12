package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidGlassColors

/**
 * Top bar for the Quem Me Deve home screen, including people management, title,
 * quick AI shortcut, theme switcher, and tab selector.
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

    Surface(
        modifier = modifier,
        color = liquid.headerSurface,
        border = BorderStroke(1.dp, liquid.cardBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Frosted Circle Avatar / People button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(liquid.circleButtonBg)
                        .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                        .clickable { onManagePeopleClick() }
                        .testTag("manage_people_top_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Pessoas e Divisões",
                        tint = liquid.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center: App Title with Liquid Pill
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Quem Me Deve",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = liquid.textPrimary
                    )
                    Text(
                        text = if (selectedTabIndex == 0) "Fatura Mensal" else "Parcelas no Cartão",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = liquid.textSecondary
                    )
                }

                // Right: Frosted Circle Theme Toggle (Moon / Sun) & AI Quick Entry
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Quick Shortcut
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                )
                            )
                            .border(1.dp, Color(0x66FFFFFF), CircleShape)
                            .clickable { onAiQuickClick() }
                            .testTag("top_bar_ai_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Entrada Rápida IA",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Theme Toggle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(liquid.circleButtonBg)
                            .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                            .clickable { onToggleThemeClick() }
                            .testTag("theme_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Alternar para Modo Claro" else "Alternar para Modo Escuro",
                            tint = liquid.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Backup & Security Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(liquid.circleButtonBg)
                            .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                            .clickable { onBackupClick() }
                            .testTag("top_bar_backup_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Backup e Segurança",
                            tint = liquid.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Floating Liquid Glass Segmented Capsule for switching tabs
            HomeTabSelector(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
        }
    }
}

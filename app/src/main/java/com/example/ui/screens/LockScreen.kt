package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLiquidGlassColors

/** Tela mostrada no lugar do app enquanto ele está bloqueado. Nada do conteúdo aparece por trás. */
@Composable
fun LockScreen(onUnlock: () -> Unit) {
    val liquid = LocalLiquidGlassColors.current
    val brand = liquid.textPrimary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(liquid.background)
            .padding(24.dp)
            .testTag("lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Acerto", style = MaterialTheme.typography.displaySmall, color = liquid.textPrimary)
                Text(".", style = MaterialTheme.typography.displaySmall, color = brand)
            }
            Text(
                "Desbloqueie com a digital ou a senha do celular.",
                style = MaterialTheme.typography.bodyLarge,
                color = liquid.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(liquid.pillPrimaryBg)
                    .clickable(role = Role.Button, onClick = onUnlock)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("unlock_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = liquid.pillPrimaryText, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Desbloquear", style = MaterialTheme.typography.titleMedium, color = liquid.pillPrimaryText)
            }
        }
    }
}

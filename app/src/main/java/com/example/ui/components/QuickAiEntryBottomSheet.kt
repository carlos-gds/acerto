package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.theme.LiquidShapes
import com.example.ui.components.glass.LiquidDialogBlur
import com.example.ui.components.glass.liquidDialogSurface
import com.example.ui.components.glass.liquidFieldMotion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAiEntryBottomSheet(
    isVisible: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmitPrompt: (String) -> Unit
) {
    if (!isVisible) return

    val liquid = LocalLiquidGlassColors.current
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var promptText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val suggestionChips = listOf(
        "Compra de 240 em 3x pra vó",
        "Tênis de 300 em 3x pro Lucas",
        "Jantar de 180 em 2x pra Ana",
        "Farmácia 75 pra Mãe"
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        sheetState = sheetState,
        containerColor = liquidDialogSurface(),
        scrimColor = liquid.scrim,
        shape = LiquidShapes.sheet
    ) {
        LiquidDialogBlur()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header with AI gradient badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            liquid.accentGradient
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Entrada Rápida via IA",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Gemini extrai descrição, parcelas e contato",
                        fontSize = 12.sp,
                        color = liquid.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggestions Chips
            Text(
                text = "Exemplos rápidos:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = liquid.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestionChips.forEach { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(liquid.cardSurfaceVariant)
                            .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable(enabled = !isLoading) {
                                promptText = chipText
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = liquid.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Input
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                modifier = Modifier.liquidFieldMotion(promptText)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("ai_prompt_input"),
                placeholder = {
                    Text(
                        text = "Ex: Compra de 240 em 3x pra vó",
                        fontSize = 14.sp,
                        color = liquid.textMuted
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (promptText.isNotBlank() && !isLoading) {
                            onSubmitPrompt(promptText)
                        }
                    }
                ),
                trailingIcon = {
                    if (promptText.isNotEmpty() && !isLoading) {
                        IconButton(onClick = { promptText = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar texto",
                                tint = liquid.textMuted
                            )
                        }
                    }
                },
                colors = liquidTextFieldColors()
            )

            // Error display if any
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Erro",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = errorMessage,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Surface(
                onClick = {
                    if (promptText.isNotBlank() && !isLoading) {
                        onSubmitPrompt(promptText)
                    }
                },
                enabled = promptText.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("ai_submit_button")
            ) {
                val isBtnActive = promptText.isNotBlank() && !isLoading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isBtnActive) {
                                Brush.horizontalGradient(
                                    listOf(liquid.accent, Color(0xFF3B82F6))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(liquid.cardSurfaceVariant, liquid.cardSurfaceVariant)
                                )
                            },
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            if (isBtnActive) Color(0x66FFFFFF) else liquid.cardBorderSubtle,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Analisando com Gemini...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isBtnActive) Color.White else liquid.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Estruturar Despesa com IA",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBtnActive) Color.White else liquid.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.BackupUiState
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.theme.LiquidShapes
import com.example.ui.components.glass.LiquidDialogBlur
import com.example.ui.components.glass.liquidDialogSurface
import com.example.ui.components.glass.liquidDialogEnter

@Composable
fun BackupSettingsDialog(
    uiState: BackupUiState,
    onDismiss: () -> Unit,
    onExportClick: () -> Unit,
    onExportCsvClick: () -> Unit = {},
    onImportClick: () -> Unit,
    onConfirmRestore: () -> Unit,
    onDismissConfirmation: () -> Unit,
    onResetStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current

    // Alert dialog when user needs to confirm destructive database replacement
    if (uiState is BackupUiState.ConfirmingRestore) {
        AlertDialog(
            modifier = Modifier.liquidDialogEnter().border(1.dp, liquid.specularBorder, LiquidShapes.cardLarge),
            onDismissRequest = onDismissConfirmation,
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                LiquidDialogBlur()
                Text(
                    text = "Substituir Dados Atuais?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = liquid.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Importar este backup substituirá todas as pessoas, despesas e parcelas atuais do aplicativo. Deseja continuar?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Conteúdo identificado: ${uiState.summary}.",
                        style = MaterialTheme.typography.labelSmall,
                        color = liquid.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmRestore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_confirm_restore")
                ) {
                    Text("Substituir e Restaurar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissConfirmation,
                    modifier = Modifier.testTag("btn_cancel_restore")
                ) {
                    Text("Cancelar", color = liquid.textSecondary)
                }
            },
            containerColor = liquidDialogSurface(),
            shape = LiquidShapes.cardLarge
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        LiquidDialogBlur()
        Surface(
            modifier = modifier
                .liquidDialogEnter()
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = LiquidShapes.cardLarge,
            color = liquidDialogSurface(),
            border = BorderStroke(1.dp, liquid.specularBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(liquid.circleButtonBg)
                                .border(1.dp, liquid.cardBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = liquid.pillPrimaryBg,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Backup e Segurança",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textPrimary
                            )
                            Text(
                                text = "Armazenamento local seguro",
                                style = MaterialTheme.typography.labelSmall,
                                color = liquid.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_backup_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar diálogo",
                            tint = liquid.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Seus dados financeiros ficam salvos exclusivamente no seu aparelho. Exporte cópias em JSON para ter backup ou migrar para outro celular.",
                    style = MaterialTheme.typography.bodySmall,
                    color = liquid.textSecondary
                )

                // Loading State Banner
                if (uiState is BackupUiState.Exporting || uiState is BackupUiState.Importing) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = liquid.cardSurfaceVariant),
                        border = BorderStroke(1.dp, liquid.cardBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = liquid.pillPrimaryBg
                            )
                            Text(
                                text = if (uiState is BackupUiState.Exporting) "Gerando arquivo de backup..." else "Validando e processando backup...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = liquid.textPrimary
                            )
                        }
                    }
                }

                // Success Banner
                if (uiState is BackupUiState.Success) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = liquid.accentPaidContainer),
                        border = BorderStroke(1.dp, liquid.accentPaid.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = liquid.accentPaid,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = uiState.message,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = liquid.textPrimary
                                )
                            }
                            IconButton(onClick = onResetStatus, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpar aviso",
                                    tint = liquid.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Error Banner
                if (uiState is BackupUiState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = uiState.message,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = liquid.textPrimary
                                )
                            }
                            IconButton(onClick = onResetStatus, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpar aviso",
                                    tint = liquid.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Action 1: Exportar Backup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = liquid.cardSurfaceVariant),
                    border = BorderStroke(1.dp, liquid.cardBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(liquid.circleButtonBg)
                                    .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = liquid.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Exportar Backup",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = liquid.textPrimary
                                )
                                Text(
                                    text = "Salvar arquivo .json com todos os dados",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = liquid.textSecondary
                                )
                            }
                        }

                        Button(
                            onClick = onExportClick,
                            enabled = uiState !is BackupUiState.Exporting && uiState !is BackupUiState.Importing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_export_backup"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = liquid.pillPrimaryBg,
                                contentColor = liquid.pillPrimaryText
                            )
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar Arquivo de Backup", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onExportCsvClick,
                            enabled = uiState !is BackupUiState.Exporting && uiState !is BackupUiState.Importing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(46.dp)
                                .testTag("btn_export_csv"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp), tint = liquid.textPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar planilha (.csv)", color = liquid.textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Action 2: Importar Backup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = liquid.cardSurfaceVariant),
                    border = BorderStroke(1.dp, liquid.cardBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(liquid.circleButtonBg)
                                    .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = liquid.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Importar Backup",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = liquid.textPrimary
                                )
                                Text(
                                    text = "Restaura pessoas, despesas e parcelas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = liquid.textSecondary
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onImportClick,
                            enabled = uiState !is BackupUiState.Exporting && uiState !is BackupUiState.Importing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_import_backup"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, liquid.specularBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = liquid.textPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecionar Arquivo para Restaurar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

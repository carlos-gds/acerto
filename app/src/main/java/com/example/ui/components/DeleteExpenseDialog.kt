package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstallmentItem
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter

@Composable
fun DeleteExpenseDialog(
    item: InstallmentItem,
    isPrivacyMode: Boolean = false,
    onDismiss: () -> Unit,
    onDeleteSingleInstallment: () -> Unit,
    onDeleteAllInstallments: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val isMultiInstallment = item.totalInstallments > 1

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = liquid.cardSurface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = if (isMultiInstallment) Icons.Default.WarningAmber else Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (isMultiInstallment) "Excluir Compra Parcelada" else "Excluir Lançamento",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = liquid.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isMultiInstallment) {
                        "A despesa \"${item.expenseTitle}\" possui ${item.totalInstallments} parcelas (${item.installmentNumber}ª de ${item.totalInstallments} neste mês de ${CurrencyFormatter.getMonthName(item.month)}).\n\nComo deseja proceder?"
                    } else {
                        "Tem certeza que deseja excluir o lançamento \"${item.expenseTitle}\" no valor de ${CurrencyFormatter.format(item.amount, isPrivacyMode)}?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
            }
        },
        confirmButton = {
            if (isMultiInstallment) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Excluir todas as parcelas
                    Button(
                        onClick = onDeleteAllInstallments,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_all_installments_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Excluir todas as ${item.totalInstallments} parcelas")
                    }

                    // Option 2: Excluir apenas esta parcela
                    OutlinedButton(
                        onClick = onDeleteSingleInstallment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_single_installment_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Excluir apenas esta parcela (${item.installmentNumber}ª)", color = liquid.textPrimary)
                    }

                    // Cancel
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar", color = liquid.textSecondary)
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancelar", color = liquid.textSecondary)
                    }

                    Button(
                        onClick = onDeleteAllInstallments,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("delete_confirm_single_btn")
                    ) {
                        Text("Excluir")
                    }
                }
            }
        },
        dismissButton = null
    )
}

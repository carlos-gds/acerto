package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLiquidGlassColors

enum class ExpenseListFilter {
    ALL,
    PENDING,
    PAID
}

@Composable
fun ExpenseSearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: ExpenseListFilter,
    onFilterChange: (ExpenseListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val filters = listOf(
        ExpenseListFilter.ALL to "Todas",
        ExpenseListFilter.PENDING to "Pendentes",
        ExpenseListFilter.PAID to "Quitadas"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Pesquisar pessoa ou compra") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisar"
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar pesquisa"
                        )
                    }
                }
            } else {
                null
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = liquid.pillPrimaryBg,
                unfocusedBorderColor = liquid.cardBorderSubtle,
                focusedContainerColor = liquid.cardSurface.copy(alpha = 0.7f),
                unfocusedContainerColor = liquid.cardSurface.copy(alpha = 0.55f),
                focusedTextColor = liquid.textPrimary,
                unfocusedTextColor = liquid.textPrimary,
                focusedPlaceholderColor = liquid.textMuted,
                unfocusedPlaceholderColor = liquid.textMuted,
                focusedLeadingIconColor = liquid.pillPrimaryBg,
                unfocusedLeadingIconColor = liquid.textMuted,
                focusedTrailingIconColor = liquid.textMuted,
                unfocusedTrailingIconColor = liquid.textMuted,
                cursorColor = liquid.pillPrimaryBg
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (filter, label) ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = liquid.cardSurfaceVariant,
                        labelColor = liquid.textSecondary,
                        selectedContainerColor = liquid.pillPrimaryBg,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = liquid.cardBorderSubtle,
                        selectedBorderColor = liquid.pillPrimaryBg
                    )
                )
            }
        }
    }
}

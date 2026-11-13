package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.GlassPill
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.components.glass.liquidFieldMotion

enum class ExpenseListFilter {
    ALL,
    PENDING,
    PAID
}

/**
 * Barra de pesquisa e filtros no estilo Liquid Glass (Figma: campo de vidro com
 * raio 12 e ícone de busca à direita; filtros em pílulas de vidro).
 */
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.liquidFieldMotion(query)
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("expense_search_field"),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text("Pesquisar pessoa ou compra", style = MaterialTheme.typography.bodyMedium)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar pesquisa"
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pesquisar"
                    )
                }
            },
            shape = LiquidShapes.field,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = liquid.accent,
                unfocusedBorderColor = liquid.cardBorder,
                focusedContainerColor = liquid.cardSurfaceVariant,
                unfocusedContainerColor = liquid.cardSurfaceVariant,
                focusedTextColor = liquid.textPrimary,
                unfocusedTextColor = liquid.textPrimary,
                focusedPlaceholderColor = liquid.textMuted,
                unfocusedPlaceholderColor = liquid.textSecondary,
                focusedTrailingIconColor = liquid.accent,
                unfocusedTrailingIconColor = liquid.textPrimary,
                cursorColor = liquid.accent
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (filter, label) ->
                val isSelected = selectedFilter == filter
                GlassPill(
                    selected = isSelected,
                    onClick = { onFilterChange(filter) },
                    modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) liquid.pillPrimaryText else liquid.textSecondary,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

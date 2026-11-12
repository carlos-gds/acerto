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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Person
import com.example.ui.theme.LocalLiquidGlassColors

val AVAILABLE_PERSON_COLORS = listOf(
    "#10B981", // Emerald
    "#3B82F6", // Blue
    "#8B5CF6", // Purple
    "#EC4899", // Pink
    "#F59E0B", // Amber
    "#06B6D4", // Cyan
    "#EF4444", // Rose
    "#6366F1"  // Indigo
)

@Composable
fun ManagePeopleDialog(
    people: List<Person>,
    onDismiss: () -> Unit,
    onAddPerson: (name: String, phone: String, colorHex: String) -> Unit,
    onDeletePerson: (Person) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current

    var isAddingNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(AVAILABLE_PERSON_COLORS.first()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = liquid.cardSurface,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.border(
            BorderStroke(1.dp, liquid.cardBorderSubtle),
            shape = RoundedCornerShape(26.dp)
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(liquid.pillPrimaryBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = liquid.pillPrimaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Amigos e Familiares",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Gerencie quem pode dividir despesas com você",
                        style = MaterialTheme.typography.labelSmall,
                        color = liquid.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isAddingNew) {
                    Text(
                        text = "Nova Pessoa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = liquid.textPrimary
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            errorText = null
                        },
                        label = { Text("Nome *", color = liquid.textSecondary) },
                        placeholder = { Text("Ex: Carlos, Vó Maria", color = liquid.textMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_person_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = liquid.textPrimary,
                            unfocusedTextColor = liquid.textPrimary,
                            focusedBorderColor = liquid.pillPrimaryBg,
                            unfocusedBorderColor = liquid.cardBorderSubtle,
                            cursorColor = liquid.pillPrimaryBg
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("WhatsApp (opcional)", color = liquid.textSecondary) },
                        placeholder = { Text("Ex: 11999998888", color = liquid.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = liquid.textPrimary,
                            unfocusedTextColor = liquid.textPrimary,
                            focusedBorderColor = liquid.pillPrimaryBg,
                            unfocusedBorderColor = liquid.cardBorderSubtle,
                            cursorColor = liquid.pillPrimaryBg
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    // Color picker row
                    Column {
                        Text(
                            text = "Cor do Avatar",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = liquid.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AVAILABLE_PERSON_COLORS.forEach { hex ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = selectedColor == hex

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedColor = hex }
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(2.dp, liquid.textPrimary, CircleShape)
                                            } else Modifier
                                        )
                                )
                            }
                        }
                    }

                    if (errorText != null) {
                        Text(
                            text = errorText!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isAddingNew = false }) {
                            Text("Cancelar", color = liquid.textSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newName.isBlank()) {
                                    errorText = "Informe o nome da pessoa."
                                    return@Button
                                }
                                onAddPerson(newName.trim(), newPhone.trim(), selectedColor)
                                newName = ""
                                newPhone = ""
                                isAddingNew = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = liquid.pillPrimaryBg,
                                contentColor = liquid.pillPrimaryText
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.testTag("confirm_save_person")
                        ) {
                            Text("Salvar", fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = liquid.cardBorderSubtle
                    )
                } else {
                    Button(
                        onClick = { isAddingNew = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = liquid.pillPrimaryBg,
                            contentColor = liquid.pillPrimaryText
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_open_add_person")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cadastrar Nova Pessoa", fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Pessoas Cadastradas (${people.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = liquid.textSecondary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(people, key = { "person_${it.id}" }) { person ->
                        val personColor = try {
                            Color(android.graphics.Color.parseColor(person.colorHex))
                        } catch (_: Exception) {
                            liquid.accentPaid
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = liquid.cardSurfaceVariant,
                            border = BorderStroke(1.dp, liquid.cardBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(personColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = person.name.firstOrNull()?.uppercase() ?: "",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = person.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = liquid.textPrimary
                                        )
                                        if (person.phone.isNotBlank()) {
                                            Text(
                                                text = person.phone,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = liquid.textSecondary
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onDeletePerson(person) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir pessoa",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = liquid.textSecondary)
            }
        }
    )
}

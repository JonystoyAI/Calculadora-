package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    title: String,
    units: List<String>,
    initialUnit1: String,
    initialUnit2: String,
    getConversionRate: (String, String) -> Double,
    extraInfo: @Composable () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    var selectedUnit1 by remember { mutableStateOf(initialUnit1) }
    var selectedUnit2 by remember { mutableStateOf(initialUnit2) }
    
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    fun formatResult(value: Double): String {
        return try {
            val bd = BigDecimal(value).setScale(8, RoundingMode.HALF_UP)
            bd.stripTrailingZeros().toPlainString()
        } catch (e: Exception) {
            value.toString()
        }
    }

    fun updateValue2(newVal1: String) {
        val doubleVal = newVal1.toDoubleOrNull()
        if (doubleVal != null) {
            isUpdating = true
            val rate = getConversionRate(selectedUnit1, selectedUnit2)
            value2 = formatResult(doubleVal * rate)
            isUpdating = false
        } else if (newVal1.isEmpty()) {
            isUpdating = true
            value2 = ""
            isUpdating = false
        }
    }

    fun updateValue1(newVal2: String) {
        val doubleVal = newVal2.toDoubleOrNull()
        if (doubleVal != null) {
            isUpdating = true
            val rate = getConversionRate(selectedUnit2, selectedUnit1)
            value1 = formatResult(doubleVal * rate)
            isUpdating = false
        } else if (newVal2.isEmpty()) {
            isUpdating = true
            value1 = ""
            isUpdating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        // First Unit Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded1,
            onExpandedChange = { expanded1 = it }
        ) {
            OutlinedTextField(
                value = selectedUnit1,
                onValueChange = {},
                readOnly = true,
                label = { Text("De") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded1,
                onDismissRequest = { expanded1 = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            selectedUnit1 = unit
                            expanded1 = false
                            updateValue2(value1)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            VoiceInputButton(onResult = { spokenText ->
                val number = spokenText.replace(Regex("[^0-9.]"), "")
                if (number.isNotEmpty()) {
                    updateValue2(number)
                    value1 = number
                }
            })
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = value1,
                onValueChange = { newVal ->
                    if (!isUpdating) {
                        value1 = newVal
                        updateValue2(newVal)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    IconButton(onClick = {
                        val pastedText = clipboardManager.getText()?.text
                        val number = pastedText?.replace(Regex("[^0-9.]"), "") ?: ""
                        if (number.isNotEmpty()) {
                            value1 = number
                            updateValue2(number)
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Pegar")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        if (value1.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(value1))
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = {
            val tempU = selectedUnit1
            selectedUnit1 = selectedUnit2
            selectedUnit2 = tempU
            updateValue2(value1)
        }) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Swap",
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Second Unit Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = { expanded2 = it }
        ) {
            OutlinedTextField(
                value = selectedUnit2,
                onValueChange = {},
                readOnly = true,
                label = { Text("A") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            selectedUnit2 = unit
                            expanded2 = false
                            updateValue1(value2)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value2,
            onValueChange = { newVal ->
                if (!isUpdating) {
                    value2 = newVal
                    updateValue1(newVal)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                IconButton(onClick = {
                    val pastedText = clipboardManager.getText()?.text
                    val number = pastedText?.replace(Regex("[^0-9.]"), "") ?: ""
                    if (number.isNotEmpty()) {
                        value2 = number
                        updateValue1(number)
                    }
                }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Pegar")
                }
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (value2.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(value2))
                    }
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        extraInfo()
    }
}


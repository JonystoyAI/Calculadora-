package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val state = viewModel.calcState.collectAsState().value
    var isScientific by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VoiceInputButton(onResult = { spokenText ->
                    val processed = spokenText
                        .replace("más", "+")
                        .replace("menos", "-")
                        .replace("por", "*")
                        .replace("entre", "/")
                        .replace(" ", "")
                    viewModel.setDisplay(processed)
                })
                IconButton(onClick = {
                    val pastedText = clipboardManager.getText()?.text
                    if (!pastedText.isNullOrEmpty()) {
                        val sanitized = pastedText.replace(Regex("[^0-9.+\\-*/^()sqrtlncosin]"), "")
                        if (sanitized.isNotEmpty()) {
                            viewModel.setDisplay(state.expression + sanitized)
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Pegar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    val textToCopy = if (state.resultPreview.isNotEmpty()) state.resultPreview else state.expression
                    if (textToCopy.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(textToCopy))
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = { isScientific = !isScientific }) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = "Científica",
                    tint = if (isScientific) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = state.expression,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light
                ),
                textAlign = TextAlign.End,
                maxLines = 2
            )
            if (state.resultPreview.isNotEmpty()) {
                Text(
                    text = "= ${state.resultPreview}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }

        val scientificButtons = listOf(
            listOf("sin(", "cos(", "tan(", "^"),
            listOf("log(", "ln(", "√", "("),
            listOf(")", "C", "⌫", "=")
        )

        val normalButtons = listOf(
            listOf("C", "√", "⌫", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        if (isScientific) {
            scientificButtons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { btn ->
                        Button(
                            onClick = { viewModel.onCalcAction(btn) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            shape = if (btn == "=") MaterialTheme.shapes.large else CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (btn in listOf("/", "*", "-", "+", "=", "C", "^", "⌫", "√"))
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (btn in listOf("/", "*", "-", "+", "=", "C", "^", "⌫", "√"))
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = btn, fontSize = 18.sp, maxLines = 1)
                        }
                    }
                }
            }
        } else {
            normalButtons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { btn ->
                        val btnWeight = if (btn == "=" && row.size == 3) 2f else 1f
                        val btnRatio = if (btn == "=" && row.size == 3) 2.2f else 1f
                        Button(
                            onClick = { viewModel.onCalcAction(btn) },
                            modifier = Modifier
                                .weight(btnWeight)
                                .aspectRatio(btnRatio),
                            shape = if (btn == "=") MaterialTheme.shapes.large else CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (btn in listOf("/", "*", "-", "+", "=", "C", "√", "⌫"))
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (btn in listOf("/", "*", "-", "+", "=", "C", "√", "⌫"))
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(text = btn, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalculatorState(
    val expression: String = "",
    val resultPreview: String = ""
)

class CalculatorViewModel : ViewModel() {
    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val exchangeRates = _exchangeRates.asStateFlow()

    private val _isFetchingRate = MutableStateFlow(false)
    val isFetchingRate = _isFetchingRate.asStateFlow()

    private val _rateError = MutableStateFlow<String?>(null)
    val rateError = _rateError.asStateFlow()

    private val _calcState = MutableStateFlow(CalculatorState())
    val calcState = _calcState.asStateFlow()

    init {
        fetchExchangeRate()
    }

    fun fetchExchangeRate() {
        viewModelScope.launch {
            _isFetchingRate.value = true
            _rateError.value = null
            try {
                val response = RetrofitInstance.api.getLatestRates()
                _exchangeRates.value = response.rates
            } catch (e: Exception) {
                _rateError.value = "Error al obtener datos: ${e.message}"
            } finally {
                _isFetchingRate.value = false
            }
        }
    }

    fun onCalcAction(action: String) {
        val currentState = _calcState.value
        when (action) {
            "C" -> {
                _calcState.value = CalculatorState(expression = "", resultPreview = "")
            }
            "⌫" -> {
                if (currentState.expression.isNotEmpty()) {
                    updateExpression(currentState.expression.dropLast(1))
                }
            }
            "=" -> {
                try {
                    val result = eval(currentState.expression)
                    _calcState.value = CalculatorState(expression = formatResult(result), resultPreview = "")
                } catch (e: Exception) {
                    _calcState.value = CalculatorState(expression = currentState.expression, resultPreview = "Error")
                }
            }
            "√" -> {
                updateExpression(currentState.expression + "sqrt(")
            }
            else -> {
                var newExpr = currentState.expression
                val operators = listOf('+', '-', '*', '/', '^')
                val isActionOp = action.length == 1 && operators.contains(action[0])
                
                if (isActionOp && newExpr.isNotEmpty()) {
                    val lastChar = newExpr.last()
                    if (operators.contains(lastChar)) {
                        newExpr = newExpr.dropLast(1) + action
                    } else {
                        newExpr += action
                    }
                } else if (action == ".") {
                    if (newExpr.isNotEmpty() && newExpr.last() == '.') {
                        // Evita punto duplicado
                    } else {
                        newExpr += action
                    }
                } else {
                    newExpr += action
                }
                
                updateExpression(newExpr)
            }
        }
    }
    
    private fun updateExpression(newExpr: String) {
        var newPreview = ""
        try {
            if (newExpr.any { it in "+-*/^" } || newExpr.contains("sqrt")) {
                val res = eval(newExpr)
                newPreview = formatResult(res)
            }
        } catch (e: Exception) {
        }
        _calcState.value = CalculatorState(expression = newExpr, resultPreview = newPreview)
    }
    
    fun setDisplay(text: String) {
        updateExpression(text)
    }

    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }
            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) {
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = str.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> Math.sqrt(x)
                        "sin" -> Math.sin(Math.toRadians(x))
                        "cos" -> Math.cos(Math.toRadians(x))
                        "tan" -> Math.tan(Math.toRadians(x))
                        "log" -> Math.log10(x)
                        "ln" -> Math.log(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                if (eat('^'.code)) x = Math.pow(x, parseFactor())
                return x
            }
        }.parse()
    }

    private fun formatResult(result: Double): String {
        val longResult = result.toLong()
        return if (result == longResult.toDouble()) {
            longResult.toString()
        } else {
            val rounded = Math.round(result * 100000000.0) / 100000000.0
            rounded.toString()
        }
    }
}

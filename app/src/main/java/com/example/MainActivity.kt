package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel)
            }
        }
    }
}

val distanceUnits = mapOf(
    "Metro (m)" to 1.0,
    "Kilómetro (km)" to 1000.0,
    "Centímetro (cm)" to 0.01,
    "Milímetro (mm)" to 0.001,
    "Milla (mi)" to 1609.344,
    "Yarda (yd)" to 0.9144,
    "Pie (ft)" to 0.3048,
    "Pulgada (in)" to 0.0254,
    "Año luz (ly)" to 9.4607304725808e15
)

val weightUnits = mapOf(
    "Gramo (g)" to 1.0,
    "Kilogramo (kg)" to 1000.0,
    "Miligramo (mg)" to 0.001,
    "Tonelada métrica (t)" to 1000000.0,
    "Libra (lb)" to 453.59237,
    "Onza (oz)" to 28.34952
)

val currencyNames = mapOf(
    "USD" to "Dólar (USD)",
    "MXN" to "Peso Mexicano (MXN)",
    "EUR" to "Euro (EUR)",
    "GBP" to "Libra Esterlina (GBP)",
    "JPY" to "Yen Japonés (JPY)",
    "ARS" to "Peso Argentino (ARS)",
    "COP" to "Peso Colombiano (COP)",
    "GTQ" to "Quetzal Guatemalteco (GTQ)",
    "CAD" to "Dólar Canadiense (CAD)",
    "KWD" to "Dinar Kuwaití (KWD)"
)

@Composable
fun MainScreen(viewModel: CalculatorViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Calc", Icons.Default.Calculate),
        TabItem("Distancia", Icons.Default.Straighten),
        TabItem("Peso", Icons.Default.Scale),
        TabItem("Divisas", Icons.Default.AttachMoney)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> CalculatorScreen(viewModel)
                1 -> UnitConverterScreen(
                    title = "Distancia",
                    units = distanceUnits.keys.toList(),
                    initialUnit1 = "Metro (m)",
                    initialUnit2 = "Kilómetro (km)",
                    getConversionRate = { u1, u2 ->
                        val val1 = distanceUnits[u1] ?: 1.0
                        val val2 = distanceUnits[u2] ?: 1.0
                        val1 / val2
                    }
                )
                2 -> UnitConverterScreen(
                    title = "Peso",
                    units = weightUnits.keys.toList(),
                    initialUnit1 = "Gramo (g)",
                    initialUnit2 = "Kilogramo (kg)",
                    getConversionRate = { u1, u2 ->
                        val val1 = weightUnits[u1] ?: 1.0
                        val val2 = weightUnits[u2] ?: 1.0
                        val1 / val2
                    }
                )
                3 -> {
                    val rates = viewModel.exchangeRates.collectAsState().value
                    val isFetching = viewModel.isFetchingRate.collectAsState().value
                    val error = viewModel.rateError.collectAsState().value

                    if (rates.isNotEmpty()) {
                        val availableCurrencies = currencyNames.keys.filter { rates.containsKey(it) }.map { currencyNames[it]!! }
                        
                        UnitConverterScreen(
                            title = "Divisas",
                            units = availableCurrencies,
                            initialUnit1 = currencyNames["USD"] ?: "",
                            initialUnit2 = currencyNames["MXN"] ?: "",
                            getConversionRate = { u1, u2 ->
                                val code1 = currencyNames.entries.find { it.value == u1 }?.key ?: "USD"
                                val code2 = currencyNames.entries.find { it.value == u2 }?.key ?: "USD"
                                val rate1 = rates[code1] ?: 1.0
                                val rate2 = rates[code2] ?: 1.0
                                rate2 / rate1
                            },
                            extraInfo = {
                                Button(onClick = { viewModel.fetchExchangeRate() }) {
                                    Text("Actualizar Tasa")
                                }
                            }
                        )
                    } else if (isFetching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (error != null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchExchangeRate() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)

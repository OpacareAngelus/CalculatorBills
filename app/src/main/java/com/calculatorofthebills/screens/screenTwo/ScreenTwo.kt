package com.calculatorofthebills.screens.screenTwo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.calculatorofthebills.CalculatorMainViewModel
import com.calculatorofthebills.LocalNavControllerProvider
import com.calculatorofthebills.ui.theme.Background
import com.calculatorofthebills.util.KeysNavigatorDestinations
import com.calculatorofthebills.util.room.Transaction
import org.koin.androidx.compose.koinViewModel

private val categories = listOf("Groceries", "Taxi", "Electronics", "Restaurant", "Other")

@Composable
fun ScreenTwo() {
    val currentContext = LocalContext.current
    val navController = LocalNavControllerProvider.current
    val calculatorMainViewModel: CalculatorMainViewModel = koinViewModel()
    val balance by calculatorMainViewModel.balance.collectAsState()

    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = amount,
                onValueChange = { value ->
                    if (value.isEmpty() || value.toDoubleOrNull()?.let { it >= 0 } == true) {
                        amount = value
                    }
                },
                label = { Text("Enter Amount (BTC)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { expanded = true }) {
                    Text(text = selectedCategory)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null) {
                        if (amountValue > balance) {
                            Toast.makeText(
                                currentContext,
                                "Insufficient balance",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            calculatorMainViewModel.addTransaction(Transaction(
                                time = System.currentTimeMillis(),
                                amount = -amountValue,
                                category = selectedCategory
                            ))
                            navController?.navigate(KeysNavigatorDestinations.screenOne)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add")
            }
        }
    }
}

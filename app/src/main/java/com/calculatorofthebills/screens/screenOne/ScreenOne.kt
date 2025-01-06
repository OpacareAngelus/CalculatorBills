package com.calculatorofthebills.screens.screenOne

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.calculatorofthebills.CalculatorMainViewModel
import com.calculatorofthebills.LocalNavControllerProvider
import com.calculatorofthebills.ui.theme.Background
import com.calculatorofthebills.util.KeysNavigatorDestinations
import com.calculatorofthebills.util.KeysStorage
import com.calculatorofthebills.util.formatTimestamp
import com.calculatorofthebills.util.model.Transaction
import com.orhanobut.hawk.Hawk
import org.koin.androidx.compose.koinViewModel

private val categories =
    listOf("All", "Incoming", "Groceries", "Taxi", "Electronics", "Restaurant", "Other")

@Composable
fun ScreenOne() {
    val navController = LocalNavControllerProvider.current

    val calculatorMainViewModel: CalculatorMainViewModel = koinViewModel()
    val thisViewModel: ScreenOneViewModel = viewModel()

    val balance = calculatorMainViewModel.balance.collectAsState().value
    val showAddDialog = calculatorMainViewModel.showAddDialog.collectAsState().value
    val bitcoinRate = thisViewModel.bitcoinRate.collectAsState().value
    val loading = thisViewModel.loading.collectAsState().value
    val transactionList = calculatorMainViewModel.transactionList.collectAsState().value
    var selectedCategory by remember { mutableStateOf("All") }

    LaunchedEffect(transactionList) {
        Hawk.put(KeysStorage.TRANSACTION_LIST, transactionList)
    }

    LaunchedEffect(balance) {
        Hawk.put(KeysStorage.BALANCE, balance)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BitcoinRateBar(loading, bitcoinRate)
            BalanceBar(balance, calculatorMainViewModel)
            AddTransactionButton(navController)
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
            TransactionList(
                transactionList = transactionList,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                }
            )
        }

        if (showAddDialog) {
            AddBalanceDialog(
                onDismiss = { calculatorMainViewModel.toggleAddDialog() },
                onAddBalance = { amount ->
                    calculatorMainViewModel.addBalance(
                        time = System.currentTimeMillis().toString(), amount = amount
                    )
                    calculatorMainViewModel.toggleAddDialog()
                })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionList(
    transactionList: List<Transaction>, selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        categories.forEach { category ->
            Button(onClick = { onCategorySelected(category) }) {
                Text(text = category)
            }
        }
    }

    val filteredTransactions = when (selectedCategory) {
        "All" -> transactionList.reversed()
        "Incoming" -> transactionList.reversed().filter { it.amount > 0 }
        else -> transactionList.reversed().filter { it.category == selectedCategory }
    }

    var displayedTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(filteredTransactions) {
        displayedTransactions = if (filteredTransactions.isEmpty()) {
            emptyList()
        } else {
            filteredTransactions.take(20)
        }
    }

    val visibleIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LaunchedEffect(visibleIndex.value) {
        if (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == displayedTransactions.size - 1) {
            if (displayedTransactions.size < filteredTransactions.size) {
                val nextTransactions =
                    filteredTransactions.drop(displayedTransactions.size).take(20)
                displayedTransactions = displayedTransactions + nextTransactions
            }
        }
    }

    if (displayedTransactions.isEmpty()) {
        Text(
            text = "No transactions available",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), state = listState
        ) {
            items(displayedTransactions) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
fun AddTransactionButton(navController: NavHostController?) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { navController?.navigate(KeysNavigatorDestinations.screenTwo) },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Add Transaction")
        }
    }
}

@Composable
fun BalanceBar(balance: Double, calculatorMainViewModel: CalculatorMainViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.82f)) {
            Text(
                text = "Balance:", style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "$balance BTC", style = MaterialTheme.typography.headlineSmall
            )
        }
        Button(onClick = { calculatorMainViewModel.toggleAddDialog() }) {
            Text(
                text = "+", style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
fun BitcoinRateBar(loading: Boolean, bitcoinRate: String) {
    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Text(
            text = "Bitcoin Rate: $bitcoinRate USD",
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun AddBalanceDialog(onDismiss: () -> Unit, onAddBalance: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val currentContext = LocalContext.current

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Balance") }, text = {
        TextField(
            value = amount,
            onValueChange = { newBalance ->
                if (newBalance.isEmpty() || newBalance.toDoubleOrNull()?.let { it >= 0 } == true) {
                    amount = newBalance
                }
            },
            label = { Text("Amount in BTC") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )
    }, confirmButton = {
        Button(onClick = {
            val amountValue = amount.toDoubleOrNull()
            if (amountValue != null) {
                onAddBalance(amountValue)
            } else {
                Toast.makeText(currentContext, "Invalid Amount", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Add")
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Time: ${formatTimestamp(transaction.time)}")
        Text("Amount: ${transaction.amount} BTC")
        Text("Category: ${transaction.category}")
    }
}

package com.calculatorofthebills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculatorofthebills.util.KeysStorage
import com.calculatorofthebills.util.model.Transaction
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CalculatorMainViewModel : ViewModel() {

    private val _transactionList = MutableStateFlow(listOf<Transaction>())
    val transactionList: StateFlow<List<Transaction>> = _transactionList

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    init {
        loadBalance()
        loadMoreTransactions()
    }

    private fun loadBalance() {
        _balance.value = Hawk.get(KeysStorage.BALANCE, 0.0)
    }

    private fun loadMoreTransactions() = viewModelScope.launch(Dispatchers.IO) {
        _transactionList.value = Hawk.get(KeysStorage.TRANSACTION_LIST, listOf())
    }

    fun addBalance(time: String, amount: Double) {
        _balance.value += amount
        _transactionList.value += Transaction(
            time = time,
            amount = amount,
            category = "Incoming"
        )
    }

    fun addTransaction(transaction: Transaction) {
        _balance.value += transaction.amount
        _transactionList.value += transaction
    }


    fun toggleAddDialog() {
        _showAddDialog.value = !_showAddDialog.value
    }
}
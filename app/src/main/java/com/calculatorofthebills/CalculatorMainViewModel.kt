package com.calculatorofthebills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import com.calculatorofthebills.util.KeysStorage
import com.calculatorofthebills.util.room.Transaction
import com.calculatorofthebills.util.room.TransactionDao
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalculatorMainViewModel : ViewModel(), KoinComponent {

    private val transactionDao: TransactionDao by inject()

    private val databaseMutex = Mutex()

    private val _transactionList = MutableStateFlow(listOf<Transaction>())
    val transactionList: StateFlow<List<Transaction>> = _transactionList

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    init {
        loadBalance()
        loadFirst20Transactions()
    }

    private fun loadFirst20Transactions() = viewModelScope.launch(Dispatchers.IO) {
        databaseMutex.withLock {
            val pagingSource = transactionDao.getAllTransactionsPaged(20, 0)
            val result = pagingSource.load(PagingSource.LoadParams.Refresh(0, 20, false))
            if (result is PagingSource.LoadResult.Page) {
                _transactionList.value = result.data
            }
        }
    }

    fun loadMoreTransactions() = viewModelScope.launch(Dispatchers.IO) {
        val offset = _transactionList.value.size
        val limit = 20

        databaseMutex.withLock {
            val pagingSource = transactionDao.getAllTransactionsPaged(limit, offset)
            val result = pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = offset,
                    loadSize = limit,
                    placeholdersEnabled = false
                )
            )

            if (result is PagingSource.LoadResult.Page) {
                _transactionList.emit(_transactionList.value + result.data)
            }
        }
    }

    private fun loadBalance() {
        _balance.value = Hawk.get(KeysStorage.BALANCE, 0.0)
    }

    fun addTransaction(transaction: Transaction) {
        _balance.value += transaction.amount
        Hawk.put(KeysStorage.BALANCE, _balance.value)
        insertTransaction(transaction)
        loadFirst20Transactions()
    }

    private fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseMutex.withLock {
                transactionDao.insertTransaction(transaction)
            }
        }
    }

    fun toggleAddDialog() {
        _showAddDialog.value = !_showAddDialog.value
    }
}

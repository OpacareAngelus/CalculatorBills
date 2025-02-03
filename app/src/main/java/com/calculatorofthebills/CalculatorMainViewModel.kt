package com.calculatorofthebills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.calculatorofthebills.util.KeysStorage
import com.calculatorofthebills.util.room.Transaction
import com.calculatorofthebills.util.room.TransactionDao
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalculatorMainViewModel : ViewModel(), KoinComponent {

    private val transactionDao: TransactionDao by inject()

    private val databaseMutex = Mutex()

    private val _pager = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
            prefetchDistance = 10
        ),
        pagingSourceFactory = { transactionDao.getAllTransactionsPaged() }
    )
    val pagedItems: MutableStateFlow<PagingData<Transaction>> = MutableStateFlow(PagingData.empty())

    init {
        viewModelScope.launch {
            _pager.flow.collectLatest { pagingData ->
                pagedItems.value = pagingData
            }
        }
    }


    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    init {
        loadBalance()
    }

    private fun loadBalance() {
        _balance.value = Hawk.get(KeysStorage.BALANCE, 0.0)
    }

    fun addTransaction(transaction: Transaction) {
        _balance.value += transaction.amount
        Hawk.put(KeysStorage.BALANCE, _balance.value)
        insertTransaction(transaction)
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

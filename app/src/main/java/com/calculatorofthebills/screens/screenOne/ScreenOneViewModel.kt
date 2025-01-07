package com.calculatorofthebills.screens.screenOne

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculatorofthebills.util.KeysStorage
import com.calculatorofthebills.util.network.RetrofitInstance
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val UPDATING_INTERVAL = 3600000L

class ScreenOneViewModel : ViewModel() {

    private val _bitcoinRate = MutableStateFlow("0.0")
    val bitcoinRate: StateFlow<String> = _bitcoinRate

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun checkAndFetchBitcoinRate() = viewModelScope.launch(Dispatchers.IO) {
        _loading.value = true
        try {
            val currentTime = System.currentTimeMillis()
            val lastUpdateTime = Hawk.get(KeysStorage.LAST_UPDATE_TIME, 0L)

            if (currentTime - lastUpdateTime > UPDATING_INTERVAL) {
                val response = RetrofitInstance.api.getCurrentBitcoinRate()
                val rate = response.bpi.USD.rate

                Hawk.put(KeysStorage.BITCOIN_RATE, rate)
                Hawk.put(KeysStorage.LAST_UPDATE_TIME, currentTime)

                _bitcoinRate.value = rate
            } else {
                val storedRate = Hawk.get(KeysStorage.BITCOIN_RATE, "0.0")
                _bitcoinRate.value = storedRate
            }
        } catch (e: Exception) {
           _bitcoinRate.value = Hawk.get(KeysStorage.BITCOIN_RATE, "0.0")
        } finally {
            _loading.value = false
        }
    }
}
package com.calculatorofthebills.util.network

import com.calculatorofthebills.util.network.response.BitcoinRateResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface BitcoinApi {
    @GET("bpi/currentprice.json")
    suspend fun getCurrentBitcoinRate(): BitcoinRateResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.coindesk.com/v1/"

    val api: BitcoinApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BitcoinApi::class.java)
    }
}

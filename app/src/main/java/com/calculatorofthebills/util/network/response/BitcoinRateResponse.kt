package com.calculatorofthebills.util.network.response

data class BitcoinRateResponse(
    val time: TimeInfo,
    val disclaimer: String,
    val chartName: String,
    val bpi: Bpi
)

data class TimeInfo(
    val updated: String,
    val updatedISO: String,
    val updateduk: String
)

data class Bpi(
    val USD: CurrencyRate,
    val GBP: CurrencyRate,
    val EUR: CurrencyRate
)

data class CurrencyRate(
    val code: String,
    val symbol: String,
    val rate: String,
    val description: String,
    val rateFloat: Double
)


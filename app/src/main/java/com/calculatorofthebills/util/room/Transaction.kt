package com.calculatorofthebills.util.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: Long,
    val amount: Double,
    val category: String
)

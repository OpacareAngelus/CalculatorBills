package com.calculatorofthebills.util.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY time DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions ORDER BY time DESC LIMIT :limit OFFSET :offset")
    fun getAllTransactionsPaged(limit: Int, offset: Int): PagingSource<Int, Transaction>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}



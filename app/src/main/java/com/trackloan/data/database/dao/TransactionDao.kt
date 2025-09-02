package com.trackloan.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackloan.data.database.converters.TransactionStatus
import com.trackloan.data.database.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY paymentDate DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    fun getTransactionById(transactionId: Long): Flow<Transaction?>

    @Query("SELECT * FROM transactions WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getTransactionsByLoanId(loanId: Long): Flow<List<Transaction>>

    @Query("SELECT t.* FROM transactions t INNER JOIN loans l ON t.loanId = l.id WHERE l.customerId = :customerId ORDER BY t.paymentDate DESC")
    fun getTransactionsByCustomerId(customerId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY paymentDate DESC")
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE transactionRef = :transactionRef")
    fun getTransactionByRef(transactionRef: String): Flow<Transaction?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: Long, status: TransactionStatus)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE status = :status")
    suspend fun getTransactionCountByStatus(status: TransactionStatus): Int

    @Query("SELECT SUM(amount) FROM transactions WHERE loanId = :loanId AND status = :status")
    suspend fun getTotalAmountByLoanIdAndStatus(loanId: Long, status: TransactionStatus): Double?

    @Query("SELECT MAX(id) FROM transactions")
    suspend fun getMaxTransactionId(): Long?
}

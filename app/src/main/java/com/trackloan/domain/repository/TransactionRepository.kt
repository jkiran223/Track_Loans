package com.trackloan.domain.repository

import com.trackloan.common.Result
import com.trackloan.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun addTransaction(transaction: Transaction): Result<Long>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(transactionId: Long): Result<Unit>
    suspend fun getTransactionById(transactionId: Long): Result<Transaction?>
    suspend fun getTransactionsByLoanId(loanId: Long): Result<List<Transaction>>
    suspend fun getTransactionsByCustomerId(customerId: Long): Result<List<Transaction>>
    suspend fun getRecentTransactions(limit: Int): Result<List<Transaction>>
    fun observeTransactionsByLoanId(loanId: Long): Flow<List<Transaction>>
    fun observeTransactionsByCustomerId(customerId: Long): Flow<List<Transaction>>
    fun observeAllTransactions(): Flow<List<Transaction>>
}

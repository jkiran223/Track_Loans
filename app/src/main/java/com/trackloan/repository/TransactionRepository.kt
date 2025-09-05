package com.trackloan.repository

import com.trackloan.common.Result
import com.trackloan.data.database.converters.TransactionStatus
import com.trackloan.data.database.dao.TransactionDao
import com.trackloan.data.database.entity.Transaction
import com.trackloan.domain.model.Transaction as DomainTransaction
import com.trackloan.domain.repository.TransactionRepository as DomainTransactionRepository
import com.trackloan.utils.TransactionRefGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) : DomainTransactionRepository {

    override suspend fun addTransaction(transaction: DomainTransaction): Result<Long> {
        return try {
            val entity = Transaction(
                id = transaction.id,
                loanId = transaction.loanId,
                amount = transaction.amount,
                paymentDate = transaction.paymentDate,
                status = TransactionStatus.valueOf(transaction.status.name),
                transactionRef = transaction.transactionRef
            )
            val id = transactionDao.insertTransaction(entity)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTransaction(transaction: DomainTransaction): Result<Unit> {
        return try {
            val entity = Transaction(
                id = transaction.id,
                loanId = transaction.loanId,
                amount = transaction.amount,
                paymentDate = transaction.paymentDate,
                status = TransactionStatus.valueOf(transaction.status.name),
                transactionRef = transaction.transactionRef
            )
            transactionDao.updateTransaction(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteTransaction(transactionId: Long): Result<Unit> {
        return try {
            transactionDao.deleteTransaction(transactionId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransactionById(transactionId: Long): Result<DomainTransaction?> {
        return try {
            val transaction = transactionDao.getTransactionById(transactionId).first()
            val domainTransaction = transaction?.let { entity: Transaction ->
                DomainTransaction(
                    id = entity.id,
                    loanId = entity.loanId,
                    amount = entity.amount,
                    paymentDate = entity.paymentDate,
                    status = com.trackloan.domain.model.TransactionStatus.valueOf(entity.status.name),
                    transactionRef = entity.transactionRef
                )
            }
            Result.Success(domainTransaction)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransactionsByLoanId(loanId: Long): Result<List<DomainTransaction>> {
        return try {
            val transactions = transactionDao.getTransactionsByLoanId(loanId).first()
            val domainTransactions = transactions.map { entity: Transaction ->
                DomainTransaction(
                    id = entity.id,
                    loanId = entity.loanId,
                    amount = entity.amount,
                    paymentDate = entity.paymentDate,
                    status = com.trackloan.domain.model.TransactionStatus.valueOf(entity.status.name),
                    transactionRef = entity.transactionRef
                )
            }
            Result.Success(domainTransactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransactionsByCustomerId(customerId: Long): Result<List<DomainTransaction>> {
        return try {
            val transactions = transactionDao.getTransactionsByCustomerId(customerId).first()
            val domainTransactions = transactions.map { entity: Transaction ->
                DomainTransaction(
                    id = entity.id,
                    loanId = entity.loanId,
                    amount = entity.amount,
                    paymentDate = entity.paymentDate,
                    status = com.trackloan.domain.model.TransactionStatus.valueOf(entity.status.name),
                    transactionRef = entity.transactionRef
                )
            }
            Result.Success(domainTransactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getRecentTransactions(limit: Int): Result<List<DomainTransaction>> {
        return try {
            val transactions = transactionDao.getAllTransactions().first().take(limit)
            val domainTransactions = transactions.map { entity: Transaction ->
                DomainTransaction(
                    id = entity.id,
                    loanId = entity.loanId,
                    amount = entity.amount,
                    paymentDate = entity.paymentDate,
                    status = com.trackloan.domain.model.TransactionStatus.valueOf(entity.status.name),
                    transactionRef = entity.transactionRef
                )
            }
            Result.Success(domainTransactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun observeTransactionsByLoanId(loanId: Long): Flow<List<DomainTransaction>> {
        return transactionDao.getTransactionsByLoanId(loanId).map { transactions: List<Transaction> ->
            transactions.map { entity: Transaction ->
                val originalStatus = com.trackloan.domain.model.TransactionStatus.valueOf(entity.status.name)
                val updatedStatus = if (originalStatus == com.trackloan.domain.model.TransactionStatus.DUE) {
                    val today = LocalDate.now()
                    val paymentDate = entity.paymentDate.toLocalDate()
                    if (paymentDate.isBefore(today)) {
                        com.trackloan.domain.model.TransactionStatus.OVERDUE
                    } else {
                        com.trackloan.domain.model.TransactionStatus.DUE
                    }
                } else {
                    originalStatus
                }
                DomainTransaction(
                    id = entity.id,
                    loanId = entity.loanId,
                    amount = entity.amount,
                    paymentDate = entity.paymentDate,
                    status = updatedStatus,
                    transactionRef = entity.transactionRef
                )
            }
        }
    }

    override fun observeTransactionsByCustomerId(customerId: Long): Flow<List<DomainTransaction>> {
        return transactionDao.getTransactionsByCustomerId(customerId).map { transactions: List<Transaction> ->
            transactions.map { entity: Transaction ->
                val originalStatus = com.trackloan.domain.model.TransactionStatus.valueOf(entity.status.name)
                val updatedStatus = if (originalStatus == com.trackloan.domain.model.TransactionStatus.DUE) {
                    val today = LocalDate.now()
                    val paymentDate = entity.paymentDate.toLocalDate()
                    if (paymentDate.isBefore(today)) {
                        com.trackloan.domain.model.TransactionStatus.OVERDUE
                    } else {
                        com.trackloan.domain.model.TransactionStatus.DUE
                    }
                } else {
                    originalStatus
                }
                DomainTransaction(
                    id = entity.id,
                    loanId = entity.loanId,
                    amount = entity.amount,
                    paymentDate = entity.paymentDate,
                    status = updatedStatus,
                    transactionRef = entity.transactionRef
                )
            }
        }
    }

    override fun observeAllTransactions(): Flow<List<DomainTransaction>> {
        return transactionDao.getAllTransactions().map { transactions: List<Transaction> ->
            transactions.map { entity: Transaction ->
                DomainTransaction(
                    id = entity.id,
                    loanId = entity.loanId,
                    amount = entity.amount,
                    paymentDate = entity.paymentDate,
                    status = com.trackloan.domain.model.TransactionStatus.valueOf(entity.status.name),
                    transactionRef = entity.transactionRef
                )
            }
        }
    }
}

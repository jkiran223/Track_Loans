package com.trackloan.domain.usecase.transaction

import com.trackloan.common.Result
import com.trackloan.domain.model.DomainError
import com.trackloan.domain.model.Transaction
import com.trackloan.domain.model.TransactionStatus
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class PayEmiUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val loanRepository: LoanRepository
) {
    suspend operator fun invoke(
        loanId: Long,
        amount: Double,
        paymentDate: LocalDateTime = LocalDateTime.now()
    ): Result<Long> {
        // Validate inputs
        val validationError = validateInputs(loanId, amount, paymentDate)
        if (validationError != null) {
            return Result.Error(Exception(validationError.toString()))
        }

        // Check if loan exists and is active
        val loanResult = loanRepository.getLoanById(loanId)
        if (loanResult is Result.Error) {
            return Result.Error(loanResult.exception)
        }

        val loan = when (loanResult) {
            is Result.Success -> loanResult.data
            else -> null
        }
        if (loan == null) {
            return Result.Error(Exception(DomainError.LoanNotFound(loanId).toString()))
        }

        if (loan.status != com.trackloan.domain.model.LoanStatus.ACTIVE) {
            return Result.Error(Exception(DomainError.LoanAlreadyClosed(loanId).toString()))
        }

        // Generate transaction reference
        val transactionRef = generateTransactionRef()

        // Create transaction
        val transaction = Transaction(
            loanId = loanId,
            transactionRef = transactionRef,
            amount = amount,
            paymentDate = paymentDate,
            status = TransactionStatus.DUE
        )

        // Add transaction to repository
        return try {
            val result = transactionRepository.addTransaction(transaction)
            when (result) {
                is Result.Success -> Result.Success(result.data)
                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Error(Exception("Unexpected loading state"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validateInputs(
        loanId: Long,
        amount: Double,
        paymentDate: LocalDateTime
    ): DomainError? {
        if (loanId <= 0) {
            return DomainError.ValidationError("loanId", "Invalid loan ID")
        }

        if (amount <= 0) {
            return DomainError.InvalidAmount(amount, "Payment amount must be positive")
        }

        if (amount > 1000000) { // Example business rule
            return DomainError.InvalidAmount(amount, "Payment amount exceeds maximum limit")
        }

        if (paymentDate.isAfter(LocalDateTime.now().plusDays(1))) {
            return DomainError.ValidationError("paymentDate", "Payment date cannot be in the future")
        }

        return null
    }

    private fun generateTransactionRef(): String {
        return "TXN${System.currentTimeMillis()}${UUID.randomUUID().toString().take(4).uppercase()}"
    }
}

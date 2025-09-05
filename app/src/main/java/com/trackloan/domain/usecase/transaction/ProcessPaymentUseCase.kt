package com.trackloan.domain.usecase.transaction

import com.trackloan.common.Result
import com.trackloan.domain.model.DomainError
import com.trackloan.domain.model.Transaction
import com.trackloan.domain.model.TransactionStatus
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class PaymentResult(
    val transactionId: Long,
    val isLastPayment: Boolean
)

class ProcessPaymentUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val loanRepository: LoanRepository
) {
    suspend operator fun invoke(
        loanId: Long,
        amount: Double,
        paymentDate: LocalDate
    ): Result<PaymentResult> {
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

        // Create transaction with payment date
        val paymentDateTime = LocalDateTime.of(paymentDate, LocalTime.now())
        val transaction = Transaction(
            loanId = loanId,
            transactionRef = transactionRef,
            amount = amount,
            paymentDate = paymentDateTime,
            status = TransactionStatus.PAID // Mark as paid immediately for payment processing
        )

        // Check if this payment will complete the loan BEFORE adding the transaction
        val isLastPayment = willCompleteLoan(loanId, amount)

        // Add transaction to repository
        val transactionResult = transactionRepository.addTransaction(transaction)
        if (transactionResult is Result.Error) {
            return Result.Error(transactionResult.exception)
        }

        val transactionId = when (transactionResult) {
            is Result.Success -> transactionResult.data
            else -> null
        }
        if (transactionId == null) {
            return Result.Error(Exception("Failed to add transaction"))
        }

        // If this is the last payment, close the loan
        if (isLastPayment) {
            val closeLoanResult = loanRepository.closeLoan(loanId)
            if (closeLoanResult is Result.Error) {
                // Log error but don't fail the payment
                // The payment was successful, just the loan status update failed
            }
        }

        return Result.Success(PaymentResult(transactionId, isLastPayment))
    }

    private fun validateInputs(
        loanId: Long,
        amount: Double,
        paymentDate: LocalDate
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

        val today = LocalDate.now()
        if (paymentDate.isAfter(today.plusDays(30))) {
            return DomainError.ValidationError("paymentDate", "Payment date cannot be more than 30 days in the future")
        }

        if (paymentDate.isBefore(today.minusDays(365))) {
            return DomainError.ValidationError("paymentDate", "Payment date cannot be more than 1 year in the past")
        }

        return null
    }

    private suspend fun willCompleteLoan(loanId: Long, currentPaymentAmount: Double): Boolean {
        // Get all completed transactions for this loan
        val transactionsResult = transactionRepository.getTransactionsByLoanId(loanId)
        if (transactionsResult is Result.Error) {
            return false
        }

        val transactions = when (transactionsResult) {
            is Result.Success -> transactionsResult.data
            else -> emptyList()
        }
        val completedTransactions = transactions.filter { it.status == TransactionStatus.PAID }

        // Get loan details
        val loanResult = loanRepository.getLoanById(loanId)
        if (loanResult is Result.Error) {
            return false
        }

        val loan = when (loanResult) {
            is Result.Success -> loanResult.data
            else -> null
        }
        if (loan == null) {
            return false
        }

        // Calculate total paid amount including current payment
        val totalPaid = completedTransactions.sumOf { it.amount } + currentPaymentAmount

        // Calculate expected total amount
        val expectedTotal = loan.emiAmount * loan.emiTenure

        // Check if this payment will complete the loan
        return totalPaid >= expectedTotal
    }

    private suspend fun checkAndUpdateLoanStatus(loanId: Long): Result<Unit> {
        // Get all completed transactions for this loan
        val transactionsResult = transactionRepository.getTransactionsByLoanId(loanId)
        if (transactionsResult is Result.Error) {
            return Result.Error(transactionsResult.exception)
        }

        val transactions = when (transactionsResult) {
            is Result.Success -> transactionsResult.data
            else -> emptyList()
        }
        val completedTransactions = transactions.filter { it.status == TransactionStatus.PAID }

        // Get loan details
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

        // Calculate total paid amount
        val totalPaid = completedTransactions.sumOf { it.amount }

        // Calculate expected total amount
        val expectedTotal = loan.emiAmount * loan.emiTenure

        // If loan is fully paid, update status to CLOSED
        if (totalPaid >= expectedTotal) {
            // Note: This would require updating the loan status in the repository
            // For now, we'll just return success as the payment was processed
        }

        return Result.Success(Unit)
    }

    private fun generateTransactionRef(): String {
        return "PAY${System.currentTimeMillis()}${java.util.UUID.randomUUID().toString().take(4).uppercase()}"
    }
}

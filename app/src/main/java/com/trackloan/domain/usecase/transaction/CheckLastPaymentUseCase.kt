package com.trackloan.domain.usecase.transaction

import com.trackloan.common.Result
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import com.trackloan.domain.model.TransactionStatus
import javax.inject.Inject

class CheckLastPaymentUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val loanRepository: LoanRepository
) {
    suspend operator fun invoke(loanId: Long, currentPaymentAmount: Double): Result<Boolean> {
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
            return Result.Error(Exception("Loan not found"))
        }

        // Calculate total paid amount including current payment
        val totalPaid = completedTransactions.sumOf { it.amount } + currentPaymentAmount

        // Calculate expected total amount
        val expectedTotal = loan.emiAmount * loan.emiTenure

        // Check if this payment will complete the loan
        return Result.Success(totalPaid >= expectedTotal)
    }
}

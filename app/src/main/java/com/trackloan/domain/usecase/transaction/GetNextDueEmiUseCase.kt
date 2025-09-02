package com.trackloan.domain.usecase.transaction

import com.trackloan.common.Result
import com.trackloan.domain.model.DomainError
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject

class GetNextDueEmiUseCase @Inject constructor(
    private val loanRepository: LoanRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(loanId: Long): Result<NextEmiData?> {
        // Validate loan ID
        if (loanId <= 0) {
            return Result.Error(Exception(DomainError.ValidationError("loanId", "Invalid loan ID").toString()))
        }

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

        if (loan.status != com.trackloan.domain.model.LoanStatus.ACTIVE) {
            return Result.Error(Exception(DomainError.LoanAlreadyClosed(loanId).toString()))
        }

        // Calculate next due EMI
        val nextEmiData = calculateNextDueEmi(loan)
        return Result.Success(nextEmiData)
    }

    private suspend fun calculateNextDueEmi(loan: com.trackloan.domain.model.Loan): NextEmiData? {
        // Get all transactions for this loan
        val transactionsResult = transactionRepository.getTransactionsByLoanId(loan.id)
        if (transactionsResult is Result.Error) {
            return null
        }

        val transactions = when (transactionsResult) {
            is Result.Success -> transactionsResult.data
            else -> emptyList()
        }

        // Calculate paid EMIs
        val paidEmiCount = transactions.count { it.status == com.trackloan.domain.model.TransactionStatus.PAID }

        // If all EMIs are paid, return null
        if (paidEmiCount >= loan.emiTenure) {
            return null
        }

        // Calculate next EMI number
        val nextEmiNumber = paidEmiCount + 1

        // Calculate due date (assuming weekly EMIs starting from EMI start date)
        val emiStartDate = loan.emiStartDate.toLocalDate()
        val dueDate = emiStartDate.plusWeeks((nextEmiNumber - 1).toLong())

        // If due date is in the past, it means it's overdue
        val today = LocalDate.now()
        val actualDueDate = if (dueDate.isBefore(today)) today else dueDate

        return NextEmiData(
            emiNumber = nextEmiNumber,
            amount = loan.emiAmount,
            dueDate = actualDueDate
        )
    }

    data class NextEmiData(
        val emiNumber: Int,
        val amount: Double,
        val dueDate: LocalDate
    )
}

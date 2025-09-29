package com.trackloan.domain.usecase.transaction

import com.trackloan.common.Result
import com.trackloan.domain.model.Loan
import com.trackloan.domain.repository.LoanRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class PostponeEmiUseCase @Inject constructor(
    private val loanRepository: LoanRepository
) {
    suspend operator fun invoke(loanId: Long, postponeByDays: Long = 7): Result<Unit> {
        if (loanId <= 0) {
            return Result.Error(Exception("Invalid loan ID"))
        }

        val loanResult = loanRepository.getLoanById(loanId)
        if (loanResult is Result.Error) {
            return Result.Error(loanResult.exception)
        }

        val loan = when (loanResult) {
            is Result.Success -> loanResult.data
            else -> null
        } ?: return Result.Error(Exception("Loan not found"))

        if (loan.status != com.trackloan.domain.model.LoanStatus.ACTIVE) {
            return Result.Error(Exception("Loan is not active"))
        }

        // Postpone emiStartDate by postponeByDays
        val newEmiStartDate = loan.emiStartDate.plus(postponeByDays, ChronoUnit.DAYS)

        val updatedLoan = loan.copy(emiStartDate = newEmiStartDate)

        return loanRepository.updateLoan(updatedLoan)
    }
}

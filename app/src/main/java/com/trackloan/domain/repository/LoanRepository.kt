package com.trackloan.domain.repository

import com.trackloan.common.Result
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.LoanSummary
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    suspend fun createLoan(loan: Loan): Result<Long>
    suspend fun updateLoan(loan: Loan): Result<Unit>
    suspend fun closeLoan(loanId: Long): Result<Unit>
    suspend fun getLoanById(loanId: Long): Result<Loan?>
    suspend fun getLoansByCustomerId(customerId: Long): Result<List<Loan>>
    suspend fun getAllLoans(): Result<List<Loan>>
    suspend fun getLoanSummary(loanId: Long): Result<LoanSummary>
    suspend fun getLoansByStatus(status: String): Result<List<Loan>>
    suspend fun getTotalLoanAmount(): Result<Double>
    fun observeLoansByCustomerId(customerId: Long): Flow<List<Loan>>
    fun observeAllLoans(): Flow<List<Loan>>
}

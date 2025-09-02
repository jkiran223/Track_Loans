package com.trackloan.repository

import com.trackloan.common.Result
import com.trackloan.data.database.converters.LoanStatus
import com.trackloan.data.database.dao.LoanDao
import com.trackloan.data.database.entity.Loan
import com.trackloan.domain.model.Loan as DomainLoan
import com.trackloan.domain.repository.LoanRepository as DomainLoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val loanDao: LoanDao
) : DomainLoanRepository {

    override suspend fun createLoan(loan: DomainLoan): Result<Long> {
        return try {
            val entity = Loan(
                id = loan.id,
                loanId = loan.loanId.toLong(),
                customerId = loan.customerId,
                loanAmount = loan.loanAmount,
                emiAmount = loan.emiAmount,
                emiTenure = loan.emiTenure,
                totalRepayment = 0.0, // TODO: Calculate this properly
                emiType = com.trackloan.data.database.converters.EmiType.valueOf(loan.emiType.name),
                emiStartDate = loan.emiStartDate,
                status = LoanStatus.valueOf(loan.status.name)
            )
            val id = loanDao.insertLoan(entity)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateLoan(loan: DomainLoan): Result<Unit> {
        return try {
            val entity = Loan(
                id = loan.id,
                loanId = loan.loanId.toLong(),
                customerId = loan.customerId,
                loanAmount = loan.loanAmount,
                emiAmount = loan.emiAmount,
                emiTenure = loan.emiTenure,
                totalRepayment = 0.0, // TODO: Calculate this properly
                emiType = com.trackloan.data.database.converters.EmiType.valueOf(loan.emiType.name),
                emiStartDate = loan.emiStartDate,
                status = LoanStatus.valueOf(loan.status.name)
            )
            loanDao.updateLoan(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun closeLoan(loanId: Long): Result<Unit> {
        return try {
            loanDao.updateLoanStatus(loanId, LoanStatus.CLOSED)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLoanById(loanId: Long): Result<DomainLoan?> {
        return try {
            val loan = loanDao.getLoanById(loanId).first()
            val domainLoan = loan?.let { entity: Loan ->
                DomainLoan(
                    id = entity.id,
                    loanId = entity.loanId.toString(),
                    customerId = entity.customerId,
                    loanAmount = entity.loanAmount,
                    emiAmount = entity.emiAmount,
                    emiTenure = entity.emiTenure,
                    emiType = com.trackloan.domain.model.EmiType.valueOf(entity.emiType.name),
                    emiStartDate = entity.emiStartDate,
                    status = com.trackloan.domain.model.LoanStatus.valueOf(entity.status.name)
                )
            }
            Result.Success(domainLoan)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLoansByCustomerId(customerId: Long): Result<List<DomainLoan>> {
        return try {
            val loans = loanDao.getLoansByCustomerId(customerId).first()
            val domainLoans = loans.map { entity: Loan ->
                DomainLoan(
                    id = entity.id,
                    loanId = entity.loanId.toString(),
                    customerId = entity.customerId,
                    loanAmount = entity.loanAmount,
                    emiAmount = entity.emiAmount,
                    emiTenure = entity.emiTenure,
                    emiType = com.trackloan.domain.model.EmiType.valueOf(entity.emiType.name),
                    emiStartDate = entity.emiStartDate,
                    status = com.trackloan.domain.model.LoanStatus.valueOf(entity.status.name)
                )
            }
            Result.Success(domainLoans)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAllLoans(): Result<List<DomainLoan>> {
        return try {
            val loans = loanDao.getAllLoans().first()
            val domainLoans = loans.map { entity: Loan ->
                DomainLoan(
                    id = entity.id,
                    loanId = entity.loanId.toString(),
                    customerId = entity.customerId,
                    loanAmount = entity.loanAmount,
                    emiAmount = entity.emiAmount,
                    emiTenure = entity.emiTenure,
                    emiType = com.trackloan.domain.model.EmiType.valueOf(entity.emiType.name),
                    emiStartDate = entity.emiStartDate,
                    status = com.trackloan.domain.model.LoanStatus.valueOf(entity.status.name)
                )
            }
            Result.Success(domainLoans)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLoanSummary(loanId: Long): Result<com.trackloan.domain.model.LoanSummary> {
        // Implementation depends on domain model and data source
        return Result.Error(Exception("Not implemented"))
    }

    override suspend fun getLoansByStatus(status: String): Result<List<DomainLoan>> {
        return try {
            val loanStatus = LoanStatus.valueOf(status)
            val loans = loanDao.getLoansByStatus(loanStatus).first()
            val domainLoans = loans.map { entity: Loan ->
                DomainLoan(
                    id = entity.id,
                    loanId = entity.loanId.toString(),
                    customerId = entity.customerId,
                    loanAmount = entity.loanAmount,
                    emiAmount = entity.emiAmount,
                    emiTenure = entity.emiTenure,
                    emiType = com.trackloan.domain.model.EmiType.valueOf(entity.emiType.name),
                    emiStartDate = entity.emiStartDate,
                    status = com.trackloan.domain.model.LoanStatus.valueOf(entity.status.name)
                )
            }
            Result.Success(domainLoans)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTotalLoanAmount(): Result<Double> {
        // Implementation depends on data source
        return Result.Error(Exception("Not implemented"))
    }

    override fun observeLoansByCustomerId(customerId: Long): Flow<List<DomainLoan>> {
        return loanDao.getLoansByCustomerId(customerId).map { loans: List<Loan> ->
            loans.map { entity: Loan ->
                DomainLoan(
                    id = entity.id,
                    loanId = entity.loanId.toString(),
                    customerId = entity.customerId,
                    loanAmount = entity.loanAmount,
                    emiAmount = entity.emiAmount,
                    emiTenure = entity.emiTenure,
                    emiType = com.trackloan.domain.model.EmiType.valueOf(entity.emiType.name),
                    emiStartDate = entity.emiStartDate,
                    status = com.trackloan.domain.model.LoanStatus.valueOf(entity.status.name)
                )
            }
        }
    }

    override fun observeAllLoans(): Flow<List<DomainLoan>> {
        return loanDao.getAllLoans().map { loans: List<Loan> ->
            loans.map { entity: Loan ->
                DomainLoan(
                    id = entity.id,
                    loanId = entity.loanId.toString(),
                    customerId = entity.customerId,
                    loanAmount = entity.loanAmount,
                    emiAmount = entity.emiAmount,
                    emiTenure = entity.emiTenure,
                    emiType = com.trackloan.domain.model.EmiType.valueOf(entity.emiType.name),
                    emiStartDate = entity.emiStartDate,
                    status = com.trackloan.domain.model.LoanStatus.valueOf(entity.status.name)
                )
            }
        }
    }
}

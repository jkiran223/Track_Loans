package com.trackloan.domain.usecase.loan

import com.trackloan.common.Result
import com.trackloan.domain.model.DomainError
import com.trackloan.domain.model.EmiType
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.LoanStatus
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.utils.TransactionRefGenerator
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class DisburseLoanUseCase @Inject constructor(
    private val loanRepository: LoanRepository,
    private val transactionRefGenerator: TransactionRefGenerator
) {
    suspend operator fun invoke(
        customerId: Long,
        loanAmount: Int,
        emiAmount: Int,
        emiTenure: Int,
        emiStartDate: LocalDate
    ): Result<Long> {
        // Validate inputs
        val validationError = validateInputs(customerId, loanAmount, emiAmount, emiTenure, emiStartDate)
        if (validationError != null) {
            return Result.Error(Exception(validationError.toString()))
        }

        // Generate loan ID as numeric string only (without "LN" prefix)
        val loanId = System.currentTimeMillis().toString()

        // Create loan entity
        val loan = Loan(
            id = 0, // Will be auto-generated
            loanId = loanId,
            customerId = customerId,
            loanAmount = loanAmount.toDouble(),
            emiAmount = emiAmount.toDouble(),
            emiTenure = emiTenure,
            emiType = EmiType.WEEKLY, // Default to weekly EMI
            emiStartDate = emiStartDate.atStartOfDay(),
            status = LoanStatus.ACTIVE,
            createdAt = LocalDateTime.now()
        )

        // Create loan
        return try {
            val result = loanRepository.createLoan(loan)
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
        customerId: Long,
        loanAmount: Int,
        emiAmount: Int,
        emiTenure: Int,
        emiStartDate: LocalDate
    ): DomainError? {
        if (customerId <= 0) {
            return DomainError.ValidationError("customerId", "Invalid customer ID")
        }

        if (loanAmount <= 0) {
            return DomainError.ValidationError("loanAmount", "Loan amount must be greater than 0")
        }

        if (loanAmount % 100 != 0) {
            return DomainError.ValidationError("loanAmount", "Loan amount must be in multiples of 100")
        }

        if (emiAmount <= 0) {
            return DomainError.ValidationError("emiAmount", "EMI amount must be greater than 0")
        }

        if (emiTenure <= 0) {
            return DomainError.ValidationError("emiTenure", "EMI tenure must be greater than 0")
        }

        if (emiTenure > 52) {
            return DomainError.ValidationError("emiTenure", "EMI tenure cannot exceed 52 weeks")
        }

        if (emiStartDate.isBefore(LocalDate.now())) {
            return DomainError.ValidationError("emiStartDate", "EMI start date must be in the future")
        }

        return null
    }
}

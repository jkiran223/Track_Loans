package com.trackloan.domain.usecase.transaction

import com.trackloan.common.Result
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.LoanStatus
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class PayEmiUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var loanRepository: LoanRepository
    private lateinit var payEmiUseCase: PayEmiUseCase

    @Before
    fun setup() {
        transactionRepository = mockk()
        loanRepository = mockk()
        payEmiUseCase = PayEmiUseCase(transactionRepository, loanRepository)
    }

    @Test
    fun `should return success when valid payment is made for active loan`() = runTest {
        // Given
        val loanId = 1L
        val amount = 5000.0
        val paymentDate = LocalDateTime.now()

        val mockLoan = Loan(
            id = loanId,
            loanId = "LOAN001",
            customerId = 1L,
            loanAmount = 100000.0,
            emiAmount = 5000.0,
            emiTenure = 24,
            emiType = com.trackloan.domain.model.EmiType.MONTHLY,
            emiStartDate = LocalDateTime.now().minusMonths(1),
            status = LoanStatus.ACTIVE,
            createdAt = LocalDateTime.now().minusMonths(2)
        )

        coEvery { loanRepository.getLoanById(loanId) } returns Result.Success(mockLoan)
        coEvery { transactionRepository.addTransaction(any()) } returns Result.Success(1L)

        // When
        val result = payEmiUseCase(loanId, amount, paymentDate)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).data)

        coVerify { loanRepository.getLoanById(loanId) }
        coVerify { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `should return error when loan is not found`() = runTest {
        // Given
        val loanId = 999L
        val amount = 5000.0

        coEvery { loanRepository.getLoanById(loanId) } returns Result.Success(null)

        // When
        val result = payEmiUseCase(loanId, amount)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("LoanNotFound") == true)

        coVerify { loanRepository.getLoanById(loanId) }
        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `should return error when loan is closed`() = runTest {
        // Given
        val loanId = 1L
        val amount = 5000.0

        val mockLoan = Loan(
            id = loanId,
            loanId = "LOAN001",
            customerId = 1L,
            loanAmount = 100000.0,
            emiAmount = 5000.0,
            emiTenure = 24,
            emiType = com.trackloan.domain.model.EmiType.MONTHLY,
            emiStartDate = LocalDateTime.now().minusMonths(1),
            status = LoanStatus.CLOSED,
            createdAt = LocalDateTime.now().minusMonths(2)
        )

        coEvery { loanRepository.getLoanById(loanId) } returns Result.Success(mockLoan)

        // When
        val result = payEmiUseCase(loanId, amount)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("LoanAlreadyClosed") == true)

        coVerify { loanRepository.getLoanById(loanId) }
        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `should return validation error when amount is negative`() = runTest {
        // Given
        val loanId = 1L
        val amount = -1000.0

        // When
        val result = payEmiUseCase(loanId, amount)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("InvalidAmount") == true)

        coVerify(exactly = 0) { loanRepository.getLoanById(any()) }
        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `should return validation error when loanId is invalid`() = runTest {
        // Given
        val loanId = 0L
        val amount = 5000.0

        // When
        val result = payEmiUseCase(loanId, amount)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("Invalid loan ID") == true)

        coVerify(exactly = 0) { loanRepository.getLoanById(any()) }
        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }
}

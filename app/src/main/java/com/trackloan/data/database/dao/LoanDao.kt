package com.trackloan.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackloan.data.database.converters.LoanStatus
import com.trackloan.data.database.entity.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Query("SELECT * FROM loans ORDER BY createdAt DESC")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    fun getLoanById(loanId: Long): Flow<Loan?>

    @Query("SELECT * FROM loans WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getLoansByCustomerId(customerId: Long): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE status = :status ORDER BY createdAt DESC")
    fun getLoansByStatus(status: LoanStatus): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE loanId = :loanId")
    fun getLoanByLoanId(loanId: Long): Flow<Loan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Query("UPDATE loans SET status = :status WHERE id = :loanId")
    suspend fun updateLoanStatus(loanId: Long, status: LoanStatus)

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoan(loanId: Long)

    @Query("SELECT COUNT(*) FROM loans")
    suspend fun getLoanCount(): Int

    @Query("SELECT COUNT(*) FROM loans WHERE status = :status")
    suspend fun getLoanCountByStatus(status: LoanStatus): Int

    @Query("SELECT MAX(loanId) FROM loans")
    suspend fun getMaxLoanId(): Long?
}

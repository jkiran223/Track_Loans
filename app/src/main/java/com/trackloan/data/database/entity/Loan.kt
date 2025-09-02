package com.trackloan.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.trackloan.data.database.converters.EmiType
import com.trackloan.data.database.converters.LoanStatus
import java.time.LocalDateTime

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["loanId"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["status"]),
        Index(value = ["emiStartDate"])
    ]
)
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long, // Unique identifier starting from 1
    val customerId: Long,
    val loanAmount: Double,
    val emiAmount: Double,
    val emiTenure: Int = 20,
    val totalRepayment: Double,
    val emiType: EmiType = EmiType.WEEKLY,
    val emiStartDate: LocalDateTime,
    val status: LoanStatus = LoanStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

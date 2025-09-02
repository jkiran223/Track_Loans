package com.trackloan.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.trackloan.data.database.converters.TransactionStatus
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionRef"], unique = true),
        Index(value = ["loanId"]),
        Index(value = ["status"]),
        Index(value = ["paymentDate"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionRef: String, // Unique, user-friendly identifier e.g. TXN0001
    val loanId: Long,
    val amount: Double,
    val paymentDate: LocalDateTime,
    val status: TransactionStatus
)

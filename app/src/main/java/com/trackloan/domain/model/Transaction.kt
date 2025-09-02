package com.trackloan.domain.model

import java.time.LocalDateTime

data class Transaction(
    val id: Long = 0,
    val transactionRef: String,
    val loanId: Long,
    val amount: Double,
    val paymentDate: LocalDateTime,
    val status: TransactionStatus = TransactionStatus.PAID
)

enum class TransactionStatus {
    PAID, DUE, OVERDUE
}

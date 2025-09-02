package com.trackloan.domain.model

import java.time.LocalDateTime

data class Loan(
    val id: Long = 0,
    val loanId: String,
    val customerId: Long,
    val loanAmount: Double,
    val emiAmount: Double,
    val emiTenure: Int,
    val emiType: EmiType,
    val emiStartDate: LocalDateTime,
    val status: LoanStatus,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class EmiType {
    DAILY, WEEKLY, MONTHLY
}

enum class LoanStatus {
    ACTIVE, CLOSED, DEFAULTED
}

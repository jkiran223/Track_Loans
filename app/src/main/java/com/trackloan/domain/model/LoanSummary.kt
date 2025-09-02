package com.trackloan.domain.model

import java.time.LocalDateTime

data class LoanSummary(
    val loanId: String,
    val customerName: String,
    val loanAmount: Double,
    val totalPaid: Double,
    val remainingAmount: Double,
    val nextEmiDate: LocalDateTime?,
    val nextEmiAmount: Double,
    val totalEmis: Int,
    val paidEmis: Int,
    val pendingEmis: Int,
    val status: LoanStatus
)

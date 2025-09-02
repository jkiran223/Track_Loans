package com.trackloan.domain.model

import java.time.LocalDateTime

data class ReportTotals(
    val totalLoans: Int,
    val totalLoanAmount: Double,
    val totalPaidAmount: Double,
    val totalPendingAmount: Double,
    val totalCustomers: Int,
    val totalTransactions: Int,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime
)

data class CustomerStatement(
    val customerId: Long,
    val customerName: String,
    val loans: List<LoanSummary>,
    val totalOutstanding: Double,
    val lastPaymentDate: LocalDateTime?
)

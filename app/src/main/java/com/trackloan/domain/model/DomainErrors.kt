package com.trackloan.domain.model

sealed class DomainError {
    // Validation Errors
    data class ValidationError(val field: String, val message: String) : DomainError()

    // Not Found Errors
    data class CustomerNotFound(val customerId: Long) : DomainError()
    data class LoanNotFound(val loanId: Long) : DomainError()
    data class TransactionNotFound(val transactionId: Long) : DomainError()

    // Business Rule Violations
    data class InsufficientFunds(val required: Double, val available: Double) : DomainError()
    data class InvalidAmount(val amount: Double, val reason: String) : DomainError()
    data class LoanAlreadyClosed(val loanId: Long) : DomainError()
    data class DuplicateTransaction(val transactionRef: String) : DomainError()
    data class InvalidDateRange(val startDate: String, val endDate: String) : DomainError()
    data class CustomerHasActiveLoans(val customerId: Long, val activeLoansCount: Int) : DomainError()

    // System Errors
    data class DatabaseError(val operation: String, val cause: String) : DomainError()
    data class NetworkError(val operation: String) : DomainError()
}

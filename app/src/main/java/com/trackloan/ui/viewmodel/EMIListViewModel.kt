package com.trackloan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.Result
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.Transaction
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EMIListViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // UI State
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Transaction data
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // Loan summary data
    private val _loanSummary = MutableStateFlow<LoanSummary?>(null)
    val loanSummary: StateFlow<LoanSummary?> = _loanSummary.asStateFlow()

    // Payment sheet state
    private val _showPaymentSheet = MutableStateFlow(false)
    val showPaymentSheet: StateFlow<Boolean> = _showPaymentSheet.asStateFlow()

    fun loadEMIList(loanId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // Load loan details
            val loan = when (val loanResult = loanRepository.getLoanById(loanId)) {
                is Result.Success -> loanResult.data
                is Result.Error -> null
                is Result.Loading -> null
            }

            // Load transactions for this loan
            transactionRepository.observeTransactionsByLoanId(loanId).collect { transactions ->
                val sortedTransactions = transactions.sortedByDescending { it.paymentDate }
                _transactions.value = sortedTransactions

                // Calculate loan summary if we have loan data
                loan?.let { loanData ->
                    _loanSummary.value = calculateLoanSummary(loanData, sortedTransactions)
                }
            }

            _isLoading.value = false
        }
    }

    private fun calculateLoanSummary(loan: Loan, transactions: List<Transaction>): LoanSummary {
        val completedTransactions = transactions.filter { it.status == com.trackloan.domain.model.TransactionStatus.PAID }
        val paidAmount = completedTransactions.sumOf { it.amount }
        val totalAmount = loan.emiAmount * loan.emiTenure
        val remainingAmount = totalAmount - paidAmount
        val completedEMIs = completedTransactions.size
        val totalEMIs = loan.emiTenure
        val progressPercentage = if (totalEMIs > 0) (completedEMIs * 100) / totalEMIs else 0

        return LoanSummary(
            loanId = loan.loanId,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            completedEMIs = completedEMIs,
            totalEMIs = totalEMIs,
            progressPercentage = progressPercentage
        )
    }

    fun showPaymentSheet() {
        _showPaymentSheet.value = true
    }

    fun dismissPaymentSheet() {
        _showPaymentSheet.value = false
    }

    data class LoanSummary(
        val loanId: String,
        val totalAmount: Double,
        val paidAmount: Double,
        val remainingAmount: Double,
        val completedEMIs: Int,
        val totalEMIs: Int,
        val progressPercentage: Int
    )
}

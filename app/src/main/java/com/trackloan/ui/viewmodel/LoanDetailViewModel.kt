package com.trackloan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.Result
import com.trackloan.common.UiState
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.Transaction
import com.trackloan.domain.repository.CustomerRepository
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanDetailViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    // UI State
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Loan data
    private val _loan = MutableStateFlow<Loan?>(null)
    val loan: StateFlow<Loan?> = _loan.asStateFlow()

    // Customer data
    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer.asStateFlow()

    // Transaction data
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // Payment sheet state
    private val _showPaymentSheet = MutableStateFlow(false)
    val showPaymentSheet: StateFlow<Boolean> = _showPaymentSheet.asStateFlow()

    fun loadLoanDetails(loanId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // Load loan details
            val loanResult = loanRepository.getLoanById(loanId)
            when (loanResult) {
                is com.trackloan.common.Result.Success<Loan?> -> {
                    _loan.value = loanResult.data

                    // Load customer details using customerId from loan
                    loanResult.data?.let { loan ->
                        val customerResult = customerRepository.getCustomerById(loan.customerId)
                        when (customerResult) {
                            is com.trackloan.common.Result.Success<Customer?> -> {
                                _customer.value = customerResult.data
                            }
                            is com.trackloan.common.Result.Error -> {
                                _customer.value = null
                            }
                            is com.trackloan.common.Result.Loading -> {
                                // Handle loading state if needed
                            }
                        }
                    }
                }
                is com.trackloan.common.Result.Error -> {
                    // Handle error - could emit error state
                    _loan.value = null
                    _customer.value = null
                }
                is com.trackloan.common.Result.Loading -> {
                    // Handle loading state if needed
                }
            }

            // Load transactions for this loan
            val transactionResult = transactionRepository.getTransactionsByLoanId(loanId)
            when (transactionResult) {
                is com.trackloan.common.Result.Success<List<Transaction>> -> {
                    // Sort transactions by payment date (newest first)
                    val sortedTransactions = transactionResult.data
                        .sortedByDescending { it.paymentDate }
                    _transactions.value = sortedTransactions
                }
                is com.trackloan.common.Result.Error -> {
                    _transactions.value = emptyList()
                }
                is com.trackloan.common.Result.Loading -> {
                    // Handle loading state if needed
                }
            }

            _isLoading.value = false
        }
    }

    fun showPaymentSheet() {
        _showPaymentSheet.value = true
    }

    fun dismissPaymentSheet() {
        _showPaymentSheet.value = false
    }
}

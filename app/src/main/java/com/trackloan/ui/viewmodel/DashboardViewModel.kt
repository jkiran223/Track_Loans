package com.trackloan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.UiState
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.LoanStatus
import com.trackloan.domain.model.Transaction
import com.trackloan.domain.model.TransactionStatus
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
class DashboardViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _loans = MutableStateFlow<List<Loan>>(emptyList())
    val loans: StateFlow<List<Loan>> = _loans.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _totalCustomers = MutableStateFlow(0)
    val totalCustomers: StateFlow<Int> = _totalCustomers.asStateFlow()

    private val _totalActiveLoans = MutableStateFlow(0)
    val totalActiveLoans: StateFlow<Int> = _totalActiveLoans.asStateFlow()

    private val _totalClosedLoans = MutableStateFlow(0)
    val totalClosedLoans: StateFlow<Int> = _totalClosedLoans.asStateFlow()

    private val _totalPendingApprovals = MutableStateFlow(0)
    val totalPendingApprovals: StateFlow<Int> = _totalPendingApprovals.asStateFlow()

    private val _emiDueToday = MutableStateFlow(0)
    val emiDueToday: StateFlow<Int> = _emiDueToday.asStateFlow()

    private val _totalPaid = MutableStateFlow(0)
    val totalPaid: StateFlow<Int> = _totalPaid.asStateFlow()

    private val _pendingPayments = MutableStateFlow(0)
    val pendingPayments: StateFlow<Int> = _pendingPayments.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                // Load customers
                customerRepository.observeAllCustomers().collect { customerList ->
                    _customers.value = customerList
                    _totalCustomers.value = customerList.size
                }

                // Load loans
                loanRepository.observeAllLoans().collect { loanList ->
                    _loans.value = loanList
                    _totalActiveLoans.value = loanList.count { it.status == LoanStatus.ACTIVE }
                    _totalClosedLoans.value = loanList.count { it.status == LoanStatus.CLOSED }
                    _totalPendingApprovals.value = loanList.count { it.status == LoanStatus.DEFAULTED }
                }

                // Load transactions
                transactionRepository.observeAllTransactions().collect { transactionList ->
                    _transactions.value = transactionList
                    _totalPaid.value = transactionList.count { it.status == TransactionStatus.PAID }
                    _pendingPayments.value = transactionList.count { it.status == TransactionStatus.DUE }
                    // TODO: Calculate EMI due today based on due dates
                    _emiDueToday.value = 0 // Placeholder
                }

                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load dashboard data")
            }
        }
    }

    fun refreshData() {
        loadDashboardData()
    }
}

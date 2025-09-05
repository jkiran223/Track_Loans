package com.trackloan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.UiState
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.Transaction
import com.trackloan.domain.repository.CustomerRepository
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionFlowViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _filteredCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val filteredCustomers: StateFlow<List<Customer>> = _filteredCustomers.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _loans = MutableStateFlow<List<Loan>>(emptyList())
    val loans: StateFlow<List<Loan>> = _loans.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _showPaymentBottomSheet = MutableStateFlow(false)
    val showPaymentBottomSheet: StateFlow<Boolean> = _showPaymentBottomSheet.asStateFlow()

    private val _selectedLoanForPayment = MutableStateFlow<Loan?>(null)
    val selectedLoanForPayment: StateFlow<Loan?> = _selectedLoanForPayment.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                customerRepository.observeAllCustomers().collect { customerList ->
                    _customers.value = customerList
                    _filteredCustomers.value = customerList
                    _uiState.value = UiState.Success(Unit)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load customers")
            }
        }
    }

    fun searchCustomers(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _filteredCustomers.value = _customers.value
        } else {
            _filteredCustomers.value = _customers.value.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                customer.mobileNumber?.contains(query) == true
            }
        }
    }

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        loadLoansForCustomer(customer.id)
        loadTransactionsForCustomer(customer.id)
    }

    private fun loadLoansForCustomer(customerId: Long) {
        viewModelScope.launch {
            try {
                loanRepository.observeLoansByCustomerId(customerId).collect { loanList ->
                    _loans.value = loanList
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load loans")
            }
        }
    }

    private fun loadTransactionsForCustomer(customerId: Long) {
        viewModelScope.launch {
            try {
                transactionRepository.observeTransactionsByCustomerId(customerId).collect { transactionList ->
                    _transactions.value = transactionList
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load transactions")
            }
        }
    }

    fun selectLoanForPayment(loan: Loan) {
        _selectedLoanForPayment.value = loan
        _showPaymentBottomSheet.value = true
    }

    fun dismissPaymentBottomSheet() {
        _showPaymentBottomSheet.value = false
        _selectedLoanForPayment.value = null
    }

    fun clearSelection() {
        _selectedCustomer.value = null
        _loans.value = emptyList()
        _transactions.value = emptyList()
        _searchQuery.value = ""
        _filteredCustomers.value = _customers.value
    }

    fun getLoanSummary(loan: Loan): String {
        val paidTransactions = _transactions.value.filter { it.loanId == loan.id && it.status == com.trackloan.domain.model.TransactionStatus.PAID }
        val totalPaid = paidTransactions.sumOf { it.amount }
        val remainingAmount = loan.loanAmount - totalPaid
        return "Paid: ₹${totalPaid}, Remaining: ₹${remainingAmount}"
    }

    fun getTransactionsForLoan(loanId: Long): List<Transaction> {
        return _transactions.value.filter { it.loanId == loanId }
            .sortedBy { it.paymentDate }
    }

    fun getLoanStatus(loanId: Long): com.trackloan.domain.model.TransactionStatus {
        val loanTransactions = getTransactionsForLoan(loanId)
        if (loanTransactions.isEmpty()) return com.trackloan.domain.model.TransactionStatus.DUE

        val hasOverdue = loanTransactions.any { it.status == com.trackloan.domain.model.TransactionStatus.OVERDUE }
        val hasDueToday = loanTransactions.any { it.status == com.trackloan.domain.model.TransactionStatus.DUE && it.paymentDate == java.time.LocalDate.now() }
        val hasDue = loanTransactions.any { it.status == com.trackloan.domain.model.TransactionStatus.DUE }
        val allPaid = loanTransactions.all { it.status == com.trackloan.domain.model.TransactionStatus.PAID }

        return when {
            hasOverdue -> com.trackloan.domain.model.TransactionStatus.OVERDUE
            hasDueToday -> com.trackloan.domain.model.TransactionStatus.DUE // This will be treated as "due today" for orange color
            allPaid || hasDue -> com.trackloan.domain.model.TransactionStatus.PAID // Green for paid or regular due
            else -> com.trackloan.domain.model.TransactionStatus.PAID
        }
    }
}

package com.trackloan.ui.viewmodel

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.Result
import com.trackloan.common.UiState
import com.trackloan.domain.model.Customer
import com.trackloan.domain.usecase.loan.DisburseLoanUseCase
import com.trackloan.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@HiltViewModel
class LoanDisbursementViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val disburseLoanUseCase: DisburseLoanUseCase
) : ViewModel() {

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Customer>>(emptyList())
    val searchResults: StateFlow<List<Customer>> = _searchResults.asStateFlow()

    // Customer selection
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    // Form data
    private val _formData = MutableStateFlow(LoanFormData())
    val formData: StateFlow<LoanFormData> = _formData.asStateFlow()

    // Form validation state
    val isFormValid: StateFlow<Boolean> = combine(
        _formData,
        _selectedCustomer
    ) { formData, selectedCustomer ->
        formData.loanAmountError == null &&
        formData.emiAmountError == null &&
        formData.emiTenureError == null &&
        formData.emiStartDateError == null &&
        formData.loanAmount.isNotEmpty() &&
        formData.emiAmount.isNotEmpty() &&
        selectedCustomer != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI state
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Dialog states
    private val _showConfirmationDialog = MutableStateFlow(false)
    val showConfirmationDialog: StateFlow<Boolean> = _showConfirmationDialog.asStateFlow()

    val showDatePickerDialog = mutableStateOf(false)
    val datePickerState = DatePickerState(
        initialSelectedDateMillis = getNextWednesday().toEpochDay() * 24 * 60 * 60 * 1000,
        initialDisplayedMonthMillis = null,
        yearRange = IntRange(2024, 2030),
        locale = Locale.getDefault()
    )

    init {
        setupSearch()
        initializeFormData()
    }

    private fun setupSearch() {
        searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotEmpty()) {
                    searchCustomers(query)
                } else {
                    _searchResults.value = emptyList()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun initializeFormData() {
        _formData.value = LoanFormData(
            emiTenure = 20,
            emiStartDate = getNextWednesday()
        )
    }

    private fun getNextWednesday(): LocalDate {
        val today = LocalDate.now()
        return if (today.dayOfWeek.value <= 3) { // Wednesday is 3
            today.with(TemporalAdjusters.next(java.time.DayOfWeek.WEDNESDAY))
        } else {
            today.with(TemporalAdjusters.next(java.time.DayOfWeek.WEDNESDAY))
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun searchCustomers(query: String) {
        viewModelScope.launch {
            try {
                val result = customerRepository.searchCustomers(query)
                when (result) {
                    is Result.Success -> _searchResults.value = result.data
                    is Result.Error -> _searchResults.value = emptyList()
                    is Result.Loading -> { /* Do nothing */ }
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun clearSelectedCustomer() {
        _selectedCustomer.value = null
        initializeFormData()
    }

    fun updateLoanAmount(amount: String) {
        val cleanAmount = amount.filter { it.isDigit() }
        _formData.value = _formData.value.copy(
            loanAmount = cleanAmount,
            loanAmountError = validateLoanAmount(cleanAmount)
        )
        updateRepayableAmount()
    }

    fun updateEmiAmount(amount: String) {
        val cleanAmount = amount.filter { it.isDigit() }
        _formData.value = _formData.value.copy(
            emiAmount = cleanAmount,
            emiAmountError = validateEmiAmount(cleanAmount)
        )
        updateRepayableAmount()
    }

    fun updateEmiTenure(tenure: String) {
        val cleanTenure = tenure.filter { it.isDigit() }.toIntOrNull() ?: 20
        _formData.value = _formData.value.copy(
            emiTenure = cleanTenure,
            emiTenureError = validateEmiTenure(cleanTenure)
        )
        updateRepayableAmount()
    }

    private fun updateRepayableAmount() {
        val loanAmount = _formData.value.loanAmount.toIntOrNull() ?: 0
        val emiAmount = _formData.value.emiAmount.toIntOrNull() ?: 0
        val tenure = _formData.value.emiTenure

        val repayableAmount = if (loanAmount > 0 && emiAmount > 0 && tenure > 0) {
            (tenure * emiAmount).toString()
        } else {
            "0"
        }

        _formData.value = _formData.value.copy(repayableAmount = repayableAmount)
    }

    fun showDatePicker() {
        showDatePickerDialog.value = true
    }

    fun dismissDatePicker() {
        showDatePickerDialog.value = false
    }

    fun confirmDateSelection() {
        datePickerState.selectedDateMillis?.let { millis ->
            val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
            _formData.value = _formData.value.copy(
                emiStartDate = selectedDate,
                emiStartDateError = validateEmiStartDate(selectedDate)
            )
        }
        showDatePickerDialog.value = false
    }

    fun showConfirmationDialog() {
        if (isFormValid()) {
            _showConfirmationDialog.value = true
        }
    }

    fun dismissConfirmationDialog() {
        _showConfirmationDialog.value = false
    }

    fun confirmLoanDisbursement() {
        val customer = _selectedCustomer.value ?: return
        val formData = _formData.value

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _showConfirmationDialog.value = false

            try {
                val result = disburseLoanUseCase(
                    customerId = customer.id,
                    loanAmount = formData.loanAmount.toInt(),
                    emiAmount = formData.emiAmount.toInt(),
                    emiTenure = formData.emiTenure,
                    emiStartDate = formData.emiStartDate
                )

                when (result) {
                    is Result.Success -> {
                        _uiState.value = UiState.Success(Unit)
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(result.exception.message ?: "Failed to disburse loan")
                    }
                    is Result.Loading -> {
                        // Do nothing
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun isFormValid(): Boolean {
        val formData = _formData.value
        return formData.loanAmountError == null &&
               formData.emiAmountError == null &&
               formData.emiTenureError == null &&
               formData.emiStartDateError == null &&
               formData.loanAmount.isNotEmpty() &&
               formData.emiAmount.isNotEmpty() &&
               _selectedCustomer.value != null
    }

    private fun validateLoanAmount(amount: String): String? {
        val amountInt = amount.toIntOrNull() ?: 0
        return when {
            amount.isEmpty() -> "Loan amount is required"
            amountInt <= 0 -> "Loan amount must be greater than 0"
            amountInt % 100 != 0 -> "Loan amount must be in multiples of 100"
            else -> null
        }
    }

    private fun validateEmiAmount(amount: String): String? {
        val amountInt = amount.toIntOrNull() ?: 0
        return when {
            amount.isEmpty() -> "EMI amount is required"
            amountInt <= 0 -> "EMI amount must be greater than 0"
            else -> null
        }
    }

    private fun validateEmiTenure(tenure: Int): String? {
        return when {
            tenure <= 0 -> "EMI tenure must be greater than 0"
            tenure > 52 -> "EMI tenure cannot exceed 52 weeks"
            else -> null
        }
    }

    private fun validateEmiStartDate(date: LocalDate): String? {
        val today = LocalDate.now()
        return when {
            date.isBefore(today) -> "EMI start date must be in the future"
            else -> null
        }
    }
}

data class LoanFormData(
    val loanAmount: String = "",
    val emiAmount: String = "",
    val emiTenure: Int = 20,
    val repayableAmount: String = "0",
    val emiStartDate: LocalDate = LocalDate.now(),
    val loanAmountError: String? = null,
    val emiAmountError: String? = null,
    val emiTenureError: String? = null,
    val emiStartDateError: String? = null
)

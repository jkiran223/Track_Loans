package com.trackloan.ui.viewmodel

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.Result
import com.trackloan.common.UiState
import com.trackloan.domain.usecase.transaction.GetNextDueEmiUseCase
import com.trackloan.domain.usecase.transaction.ProcessPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val getNextDueEmiUseCase: GetNextDueEmiUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val checkLastPaymentUseCase: com.trackloan.domain.usecase.transaction.CheckLastPaymentUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Processing time
    private val _processingTime = MutableStateFlow<Long>(0L)
    val processingTime: StateFlow<Long> = _processingTime.asStateFlow()

    // Next EMI data
    private val _nextEmi = MutableStateFlow<NextEmiData?>(null)
    val nextEmi: StateFlow<NextEmiData?> = _nextEmi.asStateFlow()

    // Payment form data
    private val _paymentForm = MutableStateFlow(PaymentFormData())
    val paymentForm: StateFlow<PaymentFormData> = _paymentForm.asStateFlow()

    // Sheet state
    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    // Dialog states
    private val _showConfirmation = MutableStateFlow(false)
    val showConfirmation: StateFlow<Boolean> = _showConfirmation.asStateFlow()

    val showDatePickerDialog = mutableStateOf(false)
    val datePickerState = DatePickerState(
        initialSelectedDateMillis = LocalDate.now().toEpochDay() * 24 * 60 * 60 * 1000,
        initialDisplayedMonthMillis = null,
        yearRange = IntRange(2024, 2030),
        locale = java.util.Locale.getDefault()
    )

    private var currentLoanId: Long = 0

    init {
        initializeFormData()
    }

    private fun initializeFormData() {
        _paymentForm.value = PaymentFormData(
            paymentDate = LocalDate.now()
        )
    }

    fun loadNextEmi(loanId: Long) {
        currentLoanId = loanId
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = getNextDueEmiUseCase(loanId)) {
                is Result.Success -> {
                    result.data?.let { emiData ->
                        _nextEmi.value = NextEmiData(
                            emiNumber = emiData.emiNumber,
                            amount = emiData.amount,
                            dueDate = emiData.dueDate
                        )
                        // Pre-fill amount with next EMI amount
                        _paymentForm.value = _paymentForm.value.copy(
                            amount = emiData.amount.toString()
                        )
                    }
                    _uiState.value = UiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.exception.message ?: "Failed to load next EMI")
                }
                is Result.Loading -> {
                    // Do nothing
                }
            }
        }
    }

    fun updatePaymentAmount(amount: String) {
        val cleanAmount = amount.filter { it.isDigit() }
        _paymentForm.value = _paymentForm.value.copy(
            amount = cleanAmount,
            amountError = validateAmount(cleanAmount)
        )
    }

    fun updatePaymentDate(date: LocalDate) {
        _paymentForm.value = _paymentForm.value.copy(
            paymentDate = date,
            dateError = validateDate(date)
        )
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
            updatePaymentDate(selectedDate)
        }
        showDatePickerDialog.value = false
    }

    fun expandSheet() {
        _isExpanded.value = true
    }

    fun collapseSheet() {
        _isExpanded.value = false
        // Reset form to next EMI values when collapsing
        _nextEmi.value?.let { emi ->
            _paymentForm.value = PaymentFormData(
                amount = emi.amount.toString(),
                paymentDate = LocalDate.now()
            )
        }
    }

    fun showQuickPayConfirmation() {
        viewModelScope.launch {
            // Check if this payment will complete the loan
            val amount = _nextEmi.value?.amount ?: 0.0
            val result = checkLastPaymentUseCase(currentLoanId, amount)
            val willCompleteLoan = when (result) {
                is Result.Success -> result.data
                else -> false
            }
            _isLastPayment.value = willCompleteLoan
            _showConfirmation.value = true
        }
    }

    fun showPaymentConfirmation() {
        if (_paymentForm.value.isValid) {
            viewModelScope.launch {
                // Check if this payment will complete the loan
                val amount = _paymentForm.value.amount.toDoubleOrNull() ?: 0.0
                val result = checkLastPaymentUseCase(currentLoanId, amount)
                val willCompleteLoan = when (result) {
                    is Result.Success -> result.data
                    else -> false
                }
                _isLastPayment.value = willCompleteLoan
                _showConfirmation.value = true
            }
        }
    }

    private fun checkIfPaymentWillCompleteLoan(amount: Double): Boolean {
        val emiNumber = _nextEmi.value?.emiNumber ?: 0
        // If the current EMI number is greater than the total EMIs, it means loan is complete
        // Fix off-by-one: check if emiNumber is exactly equal to total EMIs
        // Assuming total EMIs is emiNumber from nextEmi or from loan data
        // Here, we consider emiNumber as the next EMI to be paid, so if emiNumber > totalEmis, loan is complete
        // We need to get total EMIs from loan or nextEmi data - assuming nextEmi has emiNumber as next EMI number
        // So if emiNumber > totalEmis, then last payment done
        // But we don't have totalEmis here, so we assume emiNumber is next EMI number, so last payment is when emiNumber == totalEmis + 1
        // To fix, we need to get totalEmis from loan data or pass it here
        // For now, we assume emiNumber is next EMI number, so last payment is when emiNumber == totalEmis + 1
        // So if emiNumber > totalEmis, return true
        // Since we don't have totalEmis, we return false here and rely on backend to set isLastPayment flag
        return false
    }

    fun dismissConfirmation() {
        _showConfirmation.value = false
    }

    private val _isLastPayment = MutableStateFlow(false)
    val isLastPayment: StateFlow<Boolean> = _isLastPayment.asStateFlow()

    fun processPayment() {
        val formData = _paymentForm.value
        val isQuickPay = !_isExpanded.value
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _showConfirmation.value = false

            try {
                val amount = if (isQuickPay) {
                    _nextEmi.value?.amount ?: 0.0
                } else {
                    formData.amount.toDoubleOrNull() ?: 0.0
                }

                val paymentDate = if (isQuickPay) {
                    LocalDate.now()
                } else {
                    formData.paymentDate
                }

                val result = processPaymentUseCase(
                    loanId = currentLoanId,
                    amount = amount,
                    paymentDate = paymentDate
                )

                when (result) {
                    is Result.Success -> {
                        val elapsed = System.currentTimeMillis() - startTime
                        _processingTime.value = elapsed
                        _uiState.value = UiState.Success(Unit)
                        // Reset form
                        initializeFormData()
                        _isExpanded.value = false
                        _isLastPayment.value = result.data.isLastPayment
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(result.exception.message ?: "Payment failed")
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

    private fun validateAmount(amount: String): String? {
        val amountDouble = amount.toDoubleOrNull() ?: 0.0
        return when {
            amount.isEmpty() -> "Amount is required"
            amountDouble <= 0 -> "Amount must be greater than 0"
            amountDouble > 1000000 -> "Amount exceeds maximum limit"
            else -> null
        }
    }

    private fun validateDate(date: LocalDate): String? {
        val today = LocalDate.now()
        return when {
            date.isAfter(today.plusDays(30)) -> "Payment date cannot be more than 30 days in the future"
            date.isBefore(today.minusDays(30)) -> "Payment date cannot be more than 30 days in the past"
            else -> null
        }
    }

    data class NextEmiData(
        val emiNumber: Int,
        val amount: Double,
        val dueDate: LocalDate
    )

    data class PaymentFormData(
        val amount: String = "",
        val paymentDate: LocalDate = LocalDate.now(),
        val amountError: String? = null,
        val dateError: String? = null
    ) {
        val isValid: Boolean
            get() = amountError == null && dateError == null &&
                   amount.isNotEmpty() && amount.toDoubleOrNull() ?: 0.0 > 0
    }
}

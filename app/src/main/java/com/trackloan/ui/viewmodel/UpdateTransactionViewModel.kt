package com.trackloan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.Result
import com.trackloan.common.UiState
import com.trackloan.domain.model.DomainError
import com.trackloan.domain.model.Transaction
import com.trackloan.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class UpdateTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Update Form Data
    private val _updateForm = MutableStateFlow(UpdateFormData())
    val updateForm: StateFlow<UpdateFormData> = _updateForm.asStateFlow()

    // Confirmation Dialog State
    private val _showConfirmation = MutableStateFlow(false)
    val showConfirmation: StateFlow<Boolean> = _showConfirmation.asStateFlow()

    // Current transaction being updated
    private var currentTransaction: Transaction? = null

    fun initializeForm(transaction: Transaction) {
        currentTransaction = transaction
        _updateForm.value = UpdateFormData(
            amount = transaction.amount.toString(),
            originalAmount = transaction.amount
        )
    }

    fun updateAmount(amount: String) {
        val cleanAmount = amount.filter { it.isDigit() }
        _updateForm.value = _updateForm.value.copy(
            amount = cleanAmount,
            amountError = validateAmount(cleanAmount)
        )
    }

    fun showUpdateConfirmation() {
        if (_updateForm.value.isValid) {
            _showConfirmation.value = true
        }
    }

    fun dismissConfirmation() {
        _showConfirmation.value = false
    }

    fun updateTransaction() {
        val transaction = currentTransaction ?: return
        val formData = _updateForm.value

        if (!formData.isValid) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _showConfirmation.value = false

            try {
                val newAmount = formData.amount.toDoubleOrNull() ?: 0.0

                // Validate the new amount
                val validationError = validateUpdateAmount(newAmount, transaction.amount)
                if (validationError != null) {
                    _uiState.value = UiState.Error(validationError.toString())
                    return@launch
                }

                // Create updated transaction
                val updatedTransaction = transaction.copy(
                    amount = newAmount,
                    // Note: In a real app, you might want to track update history
                    // For now, we'll just update the amount
                )

                // Update transaction in repository
                val result = transactionRepository.updateTransaction(updatedTransaction)

                when (result) {
                    is Result.Success -> _uiState.value = UiState.Success(Unit)
                    is Result.Error -> _uiState.value = UiState.Error(result.exception.message ?: "Failed to update transaction")
                    is Result.Loading -> { /* Do nothing */ }
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

    private fun validateUpdateAmount(newAmount: Double, originalAmount: Double): DomainError? {
        return when {
            newAmount <= 0 -> DomainError.InvalidAmount(newAmount, "Amount must be positive")
            newAmount > 1000000 -> DomainError.InvalidAmount(newAmount, "Amount exceeds maximum limit")
            newAmount == originalAmount -> DomainError.ValidationError("amount", "New amount must be different from current amount")
            else -> null
        }
    }

    data class UpdateFormData(
        val amount: String = "",
        val originalAmount: Double = 0.0,
        val amountError: String? = null
    ) {
        val isValid: Boolean
            get() = amountError == null &&
                   amount.isNotEmpty() &&
                   amount.toDoubleOrNull()?.let { it > 0 && it != originalAmount } ?: false
    }
}

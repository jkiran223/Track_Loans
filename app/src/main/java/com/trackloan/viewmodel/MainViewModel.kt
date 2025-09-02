package com.trackloan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.UiState
import com.trackloan.repository.CustomerRepository
import com.trackloan.repository.LoanRepository
import com.trackloan.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                // Load basic statistics
                val customerCount = customerRepository.getCustomerCount()
                val loanCount = loanRepository.getAllLoans()

                when {
                    customerCount is com.trackloan.common.Result.Success<*> &&
                    loanCount is com.trackloan.common.Result.Success<*> -> {
                        val message = "Welcome to TrackLoan!\n" +
                                "Customers: ${customerCount.data}\n" +
                                "Loans: ${(loanCount.data as? List<*>)?.size ?: 0}"
                        _uiState.value = UiState.Success(message)
                    }
                    else -> {
                        _uiState.value = UiState.Success("Welcome to TrackLoan!")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    fun refreshData() {
        loadInitialData()
    }
}

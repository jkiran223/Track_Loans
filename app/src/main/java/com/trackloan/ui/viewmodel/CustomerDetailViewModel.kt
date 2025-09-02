package com.trackloan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.Result
import com.trackloan.common.UiState
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.Loan
import com.trackloan.repository.CustomerRepository
import com.trackloan.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer.asStateFlow()

    private val _customerLoans = MutableStateFlow<List<Loan>>(emptyList())
    val customerLoans: StateFlow<List<Loan>> = _customerLoans.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    fun loadCustomer(customerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val customerResult = customerRepository.getCustomerById(customerId)
                when (customerResult) {
                    is Result.Success -> _customer.value = customerResult.data
                    is Result.Error -> _customer.value = null
                    is Result.Loading -> { /* Handle loading state if needed */ }
                }

                val loansResult = loanRepository.getLoansByCustomerId(customerId)
                when (loansResult) {
                    is Result.Success -> _customerLoans.value = loansResult.data
                    is Result.Error -> _customerLoans.value = emptyList()
                    is Result.Loading -> { /* Handle loading state if needed */ }
                }
            } catch (e: Exception) {
                _customer.value = null
                _customerLoans.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

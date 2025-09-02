package com.trackloan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.common.UiState
import com.trackloan.domain.model.Customer
import com.trackloan.domain.usecase.customer.AddCustomerUseCase
import com.trackloan.domain.usecase.customer.UpdateCustomerUseCase
import com.trackloan.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val addCustomerUseCase: AddCustomerUseCase,
    private val updateCustomerUseCase: UpdateCustomerUseCase
) : ViewModel() {

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _addCustomerUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val addCustomerUiState: StateFlow<UiState<Unit>> = _addCustomerUiState.asStateFlow()

    init {
        observeCustomers()
        setupSearch()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        searchQuery
            .debounce(300) // Wait 300ms after user stops typing
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isEmpty()) {
                    // Switch back to observing all customers
                    observeCustomers()
                } else {
                    // Switch to search results
                    observeSearchResults(query)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSearchResults(query: String) {
        customerRepository.observeSearchCustomers(query)
            .onEach { customerList ->
                _customers.value = customerList
            }
            .catch { e ->
                _customers.value = emptyList()
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun observeCustomers() {
        customerRepository.observeAllCustomers()
            .onEach { customerList ->
                _customers.value = customerList
                _uiState.value = UiState.Success(Unit)
            }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to load customers")
            }
            .launchIn(viewModelScope)
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = customerRepository.getAllCustomers()
                when (result) {
                    is com.trackloan.common.Result.Success<*> -> {
                        _customers.value = result.data as? List<com.trackloan.domain.model.Customer> ?: emptyList()
                        _uiState.value = UiState.Success(Unit)
                    }
                    is com.trackloan.common.Result.Error -> {
                        _uiState.value = UiState.Error(result.exception.message ?: "Failed to load customers")
                    }
                    is com.trackloan.common.Result.Loading -> {
                        _uiState.value = UiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun searchCustomers(query: String) {
        viewModelScope.launch {
            try {
                val result = customerRepository.searchCustomers(query)
                when (result) {
                    is com.trackloan.common.Result.Success<*> -> {
                        _customers.value = result.data as? List<com.trackloan.domain.model.Customer> ?: emptyList()
                    }
                    is com.trackloan.common.Result.Error -> {
                        _customers.value = emptyList()
                    }
                    is com.trackloan.common.Result.Loading -> {
                        // Handle loading if needed
                    }
                }
            } catch (e: Exception) {
                _customers.value = emptyList()
            }
        }
    }

    fun addCustomer(name: String, mobileNumber: String?, address: String?) {
        viewModelScope.launch {
            _addCustomerUiState.value = UiState.Loading
            try {
                val result = addCustomerUseCase(name, mobileNumber, address)
                when (result) {
                    is com.trackloan.common.Result.Success<*> -> {
                        _addCustomerUiState.value = UiState.Success(Unit)
                        // Flow will automatically update the list
                    }
                    is com.trackloan.common.Result.Error -> {
                        _addCustomerUiState.value = UiState.Error(result.exception.message ?: "Failed to add customer")
                    }
                    is com.trackloan.common.Result.Loading -> {
                        _addCustomerUiState.value = UiState.Loading
                    }
                }
            } catch (e: Exception) {
                _addCustomerUiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun getCustomerById(customerId: Long): Customer? {
        return _customers.value.find { it.id == customerId }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            _addCustomerUiState.value = UiState.Loading
            try {
                val result = updateCustomerUseCase(
                    customerId = customer.id,
                    name = customer.name,
                    mobileNumber = customer.mobileNumber,
                    address = customer.address
                )
                when (result) {
                    is com.trackloan.common.Result.Success<*> -> {
                        _addCustomerUiState.value = UiState.Success(Unit)
                        // Flow will automatically update the list
                    }
                    is com.trackloan.common.Result.Error -> {
                        _addCustomerUiState.value = UiState.Error(result.exception.message ?: "Failed to update customer")
                    }
                    is com.trackloan.common.Result.Loading -> {
                        _addCustomerUiState.value = UiState.Loading
                    }
                }
            } catch (e: Exception) {
                _addCustomerUiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = customerRepository.deleteCustomer(customerId)
                when (result) {
                    is com.trackloan.common.Result.Success<*> -> {
                        _uiState.value = UiState.Success(Unit)
                        // Flow will automatically update the list
                    }
                    is com.trackloan.common.Result.Error -> {
                        _uiState.value = UiState.Error(result.exception.message ?: "Failed to delete customer")
                    }
                    is com.trackloan.common.Result.Loading -> {
                        // Do nothing
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun refreshCustomers() {
        loadCustomers()
    }
}

package com.trackloan.domain.usecase.customer

import com.trackloan.common.Result
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.DomainError
import com.trackloan.domain.repository.CustomerRepository
import javax.inject.Inject

class AddCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        name: String,
        mobileNumber: String? = null,
        address: String? = null
    ): Result<Long> {
        // Validate inputs
        val validationError = validateInputs(name, mobileNumber, address)
        if (validationError != null) {
            return Result.Error(Exception(validationError.toString()))
        }

        // Create customer entity
        val customer = Customer(
            name = name.trim(),
            mobileNumber = mobileNumber?.trim(),
            address = address?.trim()
        )

        // Add customer to repository
        return try {
            val result = customerRepository.addCustomer(customer)
            when (result) {
                is Result.Success -> Result.Success(result.data)
                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Error(Exception("Unexpected loading state"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validateInputs(
        name: String,
        mobileNumber: String?,
        address: String?
    ): DomainError? {
        if (name.trim().isEmpty()) {
            return DomainError.ValidationError("name", "Customer name cannot be empty")
        }

        if (name.trim().length < 2) {
            return DomainError.ValidationError("name", "Customer name must be at least 2 characters")
        }

        mobileNumber?.let {
            if (it.trim().isNotEmpty() && !isValidMobileNumber(it.trim())) {
                return DomainError.ValidationError("mobileNumber", "Invalid mobile number format")
            }
        }

        return null
    }

    private fun isValidMobileNumber(mobile: String): Boolean {
        // Basic validation - can be enhanced based on requirements
        return mobile.matches(Regex("^[+]?[0-9]{10,15}$"))
    }
}

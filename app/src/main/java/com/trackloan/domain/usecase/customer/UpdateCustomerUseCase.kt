package com.trackloan.domain.usecase.customer

import com.trackloan.common.Result
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.DomainError
import com.trackloan.domain.repository.CustomerRepository
import javax.inject.Inject

class UpdateCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        customerId: Long,
        name: String,
        mobileNumber: String?,
        address: String?
    ): Result<Unit> {
        // Validate inputs
        val validationError = validateInputs(customerId, name, mobileNumber, address)
        if (validationError != null) {
            return Result.Error(Exception(validationError.toString()))
        }

        // Check if customer exists
        val existingCustomerResult = customerRepository.getCustomerById(customerId)
        val existingCustomer = when (existingCustomerResult) {
            is Result.Success -> existingCustomerResult.data
            is Result.Error -> return Result.Error(existingCustomerResult.exception)
            is Result.Loading -> null
        }

        if (existingCustomer == null) {
            return Result.Error(Exception(DomainError.CustomerNotFound(customerId).toString()))
        }

        // Create updated customer
        val updatedCustomer = existingCustomer.copy(
            name = name.trim(),
            mobileNumber = mobileNumber?.trim()?.takeIf { it.isNotEmpty() },
            address = address?.trim()?.takeIf { it.isNotEmpty() }
        )

        // Update customer in repository
        return try {
            val result = customerRepository.updateCustomer(updatedCustomer)
            when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Error(Exception("Unexpected loading state"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validateInputs(
        customerId: Long,
        name: String,
        mobileNumber: String?,
        address: String?
    ): DomainError? {
        if (customerId <= 0) {
            return DomainError.ValidationError("customerId", "Invalid customer ID")
        }

        if (name.trim().isEmpty()) {
            return DomainError.ValidationError("name", "Name is required")
        }

        if (name.trim().length < 2) {
            return DomainError.ValidationError("name", "Name must be at least 2 characters")
        }

        if (name.trim().length > 30) {
            return DomainError.ValidationError("name", "Name cannot exceed 30 characters")
        }

        // Validate mobile number if provided
        mobileNumber?.let { mobile ->
            if (mobile.isNotEmpty()) {
                val mobileRegex = Regex("^[+]?[0-9]{10,15}$")
                if (!mobileRegex.matches(mobile.trim())) {
                    return DomainError.ValidationError("mobileNumber", "Please enter a valid mobile number")
                }
            }
        }

        // Validate address if provided
        address?.let { addr ->
            if (addr.isNotEmpty() && addr.trim().length > 50) {
                return DomainError.ValidationError("address", "Address cannot exceed 50 characters")
            }
        }

        return null
    }
}

package com.trackloan.domain.repository

import com.trackloan.common.Result
import com.trackloan.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun addCustomer(customer: Customer): Result<Long>
    suspend fun updateCustomer(customer: Customer): Result<Unit>
    suspend fun deleteCustomer(customerId: Long): Result<Unit>
    suspend fun getCustomerById(customerId: Long): Result<Customer?>
    suspend fun getAllCustomers(): Result<List<Customer>>
    suspend fun searchCustomers(query: String): Result<List<Customer>>
    suspend fun getCustomerCount(): Result<Int>
    fun observeAllCustomers(): Flow<List<Customer>>
    fun observeCustomerById(customerId: Long): Flow<Customer?>
    fun observeSearchCustomers(query: String): Flow<List<Customer>>
}

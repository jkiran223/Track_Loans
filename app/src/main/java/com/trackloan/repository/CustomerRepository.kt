package com.trackloan.repository

import com.trackloan.data.database.dao.CustomerDao
import com.trackloan.data.database.entity.Customer
import com.trackloan.domain.model.Customer as DomainCustomer
import com.trackloan.domain.repository.CustomerRepository as DomainCustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.trackloan.common.Result

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) : DomainCustomerRepository {

    override suspend fun addCustomer(customer: DomainCustomer): Result<Long> {
        return try {
            val entity = Customer(
                name = customer.name,
                mobileNumber = customer.mobileNumber,
                address = customer.address
            )
            val id = customerDao.insertCustomer(entity)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateCustomer(customer: DomainCustomer): Result<Unit> {
        return try {
            val entity = Customer(
                id = customer.id,
                name = customer.name,
                mobileNumber = customer.mobileNumber,
                address = customer.address
            )
            customerDao.updateCustomer(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteCustomer(customerId: Long): Result<Unit> {
        return try {
            customerDao.deleteCustomer(customerId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCustomerById(customerId: Long): Result<DomainCustomer?> {
        return try {
            val customer = customerDao.getCustomerById(customerId).first()
            val domainCustomer = customer?.let { entity: Customer ->
                DomainCustomer(
                    id = entity.id,
                    name = entity.name,
                    mobileNumber = entity.mobileNumber,
                    address = entity.address
                )
            }
            Result.Success(domainCustomer)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAllCustomers(): Result<List<DomainCustomer>> {
        return try {
            val customers = customerDao.getAllCustomers().first()
            val domainCustomers = customers.map { entity: Customer ->
                DomainCustomer(
                    id = entity.id,
                    name = entity.name,
                    mobileNumber = entity.mobileNumber,
                    address = entity.address
                )
            }
            Result.Success(domainCustomers)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun searchCustomers(query: String): Result<List<DomainCustomer>> {
        return try {
            val customers = customerDao.searchCustomers(query).first()
            val domainCustomers = customers.map { entity: Customer ->
                DomainCustomer(
                    id = entity.id,
                    name = entity.name,
                    mobileNumber = entity.mobileNumber,
                    address = entity.address
                )
            }
            Result.Success(domainCustomers)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCustomerCount(): Result<Int> {
        return try {
            val count = customerDao.getCustomerCount()
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun observeAllCustomers(): Flow<List<DomainCustomer>> {
        return customerDao.getAllCustomers().map { customers: List<Customer> ->
            customers.map { entity: Customer ->
                DomainCustomer(
                    id = entity.id,
                    name = entity.name,
                    mobileNumber = entity.mobileNumber,
                    address = entity.address
                )
            }
        }
    }

    override fun observeCustomerById(customerId: Long): Flow<DomainCustomer?> {
        return customerDao.getCustomerById(customerId).map { customer: Customer? ->
            customer?.let { entity: Customer ->
                DomainCustomer(
                    id = entity.id,
                    name = entity.name,
                    mobileNumber = entity.mobileNumber,
                    address = entity.address
                )
            }
        }
    }

    override fun observeSearchCustomers(query: String): Flow<List<DomainCustomer>> {
        return customerDao.searchCustomers(query).map { customers: List<Customer> ->
            customers.map { entity: Customer ->
                DomainCustomer(
                    id = entity.id,
                    name = entity.name,
                    mobileNumber = entity.mobileNumber,
                    address = entity.address
                )
            }
        }
    }
}

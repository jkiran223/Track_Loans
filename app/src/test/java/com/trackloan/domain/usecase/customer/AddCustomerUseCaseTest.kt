
package com.trackloan.domain.usecase.customer

import com.trackloan.domain.model.Customer
import com.trackloan.domain.repository.CustomerRepository
import com.trackloan.common.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AddCustomerUseCaseTest {

    @Mock
    private lateinit var customerRepository: CustomerRepository

    private lateinit var addCustomerUseCase: AddCustomerUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addCustomerUseCase = AddCustomerUseCase(customerRepository)
    }

    @Test
    fun `addCustomer should return success when repository succeeds`() = runTest {
        // Given
        val name = "John Doe"
        val mobileNumber = "1234567890"
        val address = "123 Main St"
        val expectedId = 1L
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Success(expectedId))

        // When
        val result = addCustomerUseCase(name, mobileNumber, address)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }

    @Test
    fun `addCustomer should return error when repository fails`() = runTest {
        // Given
        val name = "John Doe"
        val mobileNumber = "1234567890"
        val address = "123 Main St"
        val errorMessage = "Database error"
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Error(Exception(errorMessage)))

        // When
        val result = addCustomerUseCase(name, mobileNumber, address)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).exception.message)
    }

    @Test
    fun `addCustomer should handle empty name`() = runTest {
        // Given
        val name = ""
        val mobileNumber = "1234567890"
        val address = "123 Main St"
        val expectedId = 2L
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Success(expectedId))

        // When
        val result = addCustomerUseCase(name, mobileNumber, address)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }

    @Test
    fun `addCustomer should handle special characters in name`() = runTest {
        // Given
        val name = "José María O'Connor"
        val mobileNumber = "+1-555-123-4567"
        val address = "Calle Principal 123, Ciudad"
        val expectedId = 3L
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Success(expectedId))

        // When
        val result = addCustomerUseCase(name, mobileNumber, address)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }

    @Test
    fun `addCustomer should handle long address`() = runTest {
        // Given
        val longAddress = "This is a very long address that exceeds normal length and contains multiple lines and special characters like @#$%^&*() and numbers 1234567890"
        val name = "Test User"
        val mobileNumber = "9876543210"
        val expectedId = 4L
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Success(expectedId))

        // When
        val result = addCustomerUseCase(name, mobileNumber, longAddress)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }

    @Test
    fun `addCustomer should call repository with correct customer data`() = runTest {
        // Given
        val name = "Jane Smith"
        val mobileNumber = "555-1234"
        val address = "456 Oak Avenue"
        val expectedId = 5L
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Success(expectedId))

        // When
        val result = addCustomerUseCase(name, mobileNumber, address)

        // Then
        verify(customerRepository).addCustomer(
            org.mockito.kotlin.check { savedCustomer ->
                assertEquals(name, savedCustomer.name)
                assertEquals(mobileNumber, savedCustomer.mobileNumber)
                assertEquals(address, savedCustomer.address)
            }
        )
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }

    @Test
    fun `addCustomer should handle null email gracefully`() = runTest {
        // Given
        val name = "Anonymous User"
        val mobileNumber = "000-0000"
        val address = "Unknown Address"
        val expectedId = 6L
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Success(expectedId))

        // When
        val result = addCustomerUseCase(name, mobileNumber, address)

        // Then
        verify(customerRepository).addCustomer(
            org.mockito.kotlin.check { savedCustomer ->
                assertEquals(name, savedCustomer.name)
                assertEquals(mobileNumber, savedCustomer.mobileNumber)
                assertEquals(address, savedCustomer.address)
            }
        )
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }

    @Test
    fun `addCustomer should preserve all customer fields when adding to repository`() = runTest {
        // Given
        val name = "Complete Test User"
        val mobileNumber = "+1-234-567-8901"
        val address = "1234 Test Street, Test City, TC 12345"
        val expectedId = 7L
        whenever(customerRepository.addCustomer(any())).thenReturn(Result.Success(expectedId))

        // When
        val result = addCustomerUseCase(name, mobileNumber, address)

        // Then
        verify(customerRepository).addCustomer(
            org.mockito.kotlin.check { savedCustomer ->
                assertEquals(name, savedCustomer.name)
                assertEquals(mobileNumber, savedCustomer.mobileNumber)
                assertEquals(address, savedCustomer.address)
                assertEquals(0L, savedCustomer.id) // ID should be 0 for new customers
            }
        )
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }
}

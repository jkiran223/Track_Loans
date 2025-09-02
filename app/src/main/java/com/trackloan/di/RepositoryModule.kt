package com.trackloan.di

import com.trackloan.domain.repository.CustomerRepository
import com.trackloan.domain.repository.LoanRepository
import com.trackloan.domain.repository.TransactionRepository
import com.trackloan.repository.CustomerRepository as CustomerRepositoryImpl
import com.trackloan.repository.LoanRepository as LoanRepositoryImpl
import com.trackloan.repository.TransactionRepository as TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindLoanRepository(
        loanRepositoryImpl: LoanRepositoryImpl
    ): LoanRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository
}

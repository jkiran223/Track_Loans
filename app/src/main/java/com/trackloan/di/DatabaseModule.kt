package com.trackloan.di

import android.content.Context
import androidx.room.Room
import com.trackloan.data.database.AppDatabase
import com.trackloan.data.database.dao.CustomerDao
import com.trackloan.data.database.dao.LoanDao
import com.trackloan.data.database.dao.TransactionDao
import com.trackloan.utils.TransactionRefGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "trackloan_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    @Singleton
    fun provideLoanDao(database: AppDatabase): LoanDao {
        return database.loanDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRefGenerator(): TransactionRefGenerator {
        return TransactionRefGenerator
    }
}

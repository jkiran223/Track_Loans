package com.trackloan.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trackloan.data.database.converters.Converters
import com.trackloan.data.database.dao.CustomerDao
import com.trackloan.data.database.dao.LoanDao
import com.trackloan.data.database.dao.TransactionDao
import com.trackloan.data.database.entity.Customer
import com.trackloan.data.database.entity.Loan
import com.trackloan.data.database.entity.Transaction

@Database(
    entities = [
        Customer::class,
        Loan::class,
        Transaction::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun loanDao(): LoanDao
    abstract fun transactionDao(): TransactionDao
}

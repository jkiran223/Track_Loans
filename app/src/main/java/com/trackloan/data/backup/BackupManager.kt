package com.trackloan.data.backup

import android.content.Context
import android.net.Uri
import com.trackloan.data.database.AppDatabase
import com.trackloan.data.database.entity.Customer
import com.trackloan.data.database.entity.Loan
import com.trackloan.data.database.entity.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

data class BackupData(
    val customers: List<Customer>,
    val loans: List<Loan>,
    val transactions: List<Transaction>
) : Serializable

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {

    suspend fun backupToUri(uri: Uri) = withContext(Dispatchers.IO) {
        val customers = database.customerDao().getAllCustomersList()
        val loans = database.loanDao().getAllLoansList()
        val transactions = database.transactionDao().getAllTransactionsList()

        val backupData = BackupData(customers, loans, transactions)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ObjectOutputStream(outputStream).use { oos ->
                oos.writeObject(backupData)
            }
        }
    }

    suspend fun restoreFromUri(uri: Uri) = withContext(Dispatchers.IO) {
        val backupData: BackupData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ObjectInputStream(inputStream).use { ois ->
                ois.readObject() as BackupData
            }
        } ?: throw IllegalArgumentException("Unable to open input stream for URI")

        // Clear existing data
        database.customerDao().deleteAll()
        database.loanDao().deleteAll()
        database.transactionDao().deleteAll()

        // Insert backup data
        database.customerDao().insertAll(backupData.customers)
        database.loanDao().insertAll(backupData.loans)
        database.transactionDao().insertAll(backupData.transactions)
    }
}

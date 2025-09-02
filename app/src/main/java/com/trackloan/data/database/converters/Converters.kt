package com.trackloan.data.database.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }

    @TypeConverter
    fun fromEmiType(value: String?): EmiType {
        return EmiType.valueOf(value ?: EmiType.WEEKLY.name)
    }

    @TypeConverter
    fun emiTypeToString(emiType: EmiType): String {
        return emiType.name
    }

    @TypeConverter
    fun fromLoanStatus(value: String?): LoanStatus {
        return LoanStatus.valueOf(value ?: LoanStatus.ACTIVE.name)
    }

    @TypeConverter
    fun loanStatusToString(status: LoanStatus): String {
        return status.name
    }

    @TypeConverter
    fun fromTransactionStatus(value: String?): TransactionStatus {
        return TransactionStatus.valueOf(value ?: TransactionStatus.DUE.name)
    }

    @TypeConverter
    fun transactionStatusToString(status: TransactionStatus): String {
        return status.name
    }
}

enum class EmiType {
    WEEKLY, MONTHLY
}

enum class LoanStatus {
    ACTIVE, CLOSED
}

enum class TransactionStatus {
    PAID, DUE, OVERDUE
}

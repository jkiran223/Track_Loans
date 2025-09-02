package com.trackloan.utils

object TransactionRefGenerator {

    private const val PREFIX = "TXN"
    private const val PADDING_LENGTH = 4

    /**
     * Generates a unique transaction reference using the row ID
     * Format: TXN0001, TXN0002, etc.
     */
    fun generateTransactionRef(rowId: Long): String {
        return "$PREFIX${rowId.toString().padStart(PADDING_LENGTH, '0')}"
    }

    /**
     * Extracts the row ID from a transaction reference
     * Returns null if the format is invalid
     */
    fun extractRowId(transactionRef: String): Long? {
        return try {
            if (transactionRef.startsWith(PREFIX)) {
                val numberPart = transactionRef.substring(PREFIX.length)
                numberPart.toLong()
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Validates if a transaction reference has the correct format
     */
    fun isValidTransactionRef(transactionRef: String): Boolean {
        return transactionRef.startsWith(PREFIX) &&
               transactionRef.length == PREFIX.length + PADDING_LENGTH &&
               transactionRef.substring(PREFIX.length).all { it.isDigit() }
    }
}

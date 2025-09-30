# Fix Transaction Screen Default Filter Issue

## Problem
On the transaction screen, the default selected filter is "Due Today" but no customer list is visible until the user changes the filter to another value and then comes back to "Due Today."

## Root Cause
The filtering logic depends on loan data to determine customers with EMIs due today, but this data is loaded asynchronously after the initial filter application in the ViewModel.

## Solution
Modify TransactionFlowViewModel.kt to ensure applyCustomerFilter() is called after the loans data is loaded.

## Steps
- [x] Modify loadAllLoansAndTransactions() in TransactionFlowViewModel.kt to call applyCustomerFilter() when loans are collected.
- [x] Test the fix by navigating from dashboard loan card to transaction screen.

## Status
Completed

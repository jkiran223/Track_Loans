# TODO List for Transaction Color Coding and Logic Implementation

## Color Coding Implementation
- [x] Add Green and Red colors to Color.kt
- [x] Update TransactionStatusChip in EMIListScreen.kt to use custom colors:
  - PAID: Green
  - DUE: Orange
  - OVERDUE: Red

## Logic Implementation
- [x] Implement due date logic: EMI is DUE until payment date is today, then OVERDUE if past
- [x] Update transaction status based on current date when loading transactions
- [x] Ensure after payment, EMI is marked as PAID and loan turns green (already partially implemented)

## Loan List Color Coding
- [x] Add logic to determine loan status based on transactions
- [x] Apply color coding to loan list items (OVERDUE: Red, DUE TODAY: Orange, DUE/PAID: Green)
- [x] Update TransactionFlowViewModel with getLoanStatus method
- [x] Update TransactionFlowScreen to use loan status for color coding

## Testing
- [x] Test color changes in UI
- [x] Test status update logic
- [x] Verify payment flow updates status correctly

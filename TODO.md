# TODO - Payment Processing with Last Payment Detection and UI Update

- [x] Update ProcessPaymentUseCase to return PaymentResult with isLastPayment flag
- [x] Update PaymentViewModel to expose isLastPayment state and update it after payment processing
- [x] Update PaymentBottomSheet composable to show different confirmation dialog title and button text if last payment
- [x] Update PaymentConfirmationDialog composable to accept isLastPayment and adjust UI accordingly
- [x] Fix last payment detection logic to check BEFORE adding transaction
- [x] Fix isLastPayment flag to be set correctly based on actual last payment detection
- [x] Implement loan status update to CLOSED when last payment is made
- [ ] Run the app and test payment flow:
  - [ ] Test normal payment flow
  - [ ] Test last payment flow and verify UI shows "Confirm and Close Loan"
  - [ ] Verify loan status updates correctly to CLOSED after last payment
- [ ] Fix any issues found during testing
- [ ] Prepare for code review and merge

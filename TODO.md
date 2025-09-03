# TODO: Fix Duplicate Success Message and Adjust Delay

## Tasks
- [x] Edit PaymentViewModel.kt to measure payment processing time and store it in state
- [x] Edit PaymentBottomSheet.kt to delay onPaymentSuccess call based on processing time
- [x] Edit TransactionFlowScreen.kt to ensure success message is shown only once
- [ ] Test the changes to verify no duplicate messages and proper delay

## Details
- Measure processing time from start of processPayment to success
- Delay success message by the processing time to make delay depend on payment process
- Ensure only one success message is shown

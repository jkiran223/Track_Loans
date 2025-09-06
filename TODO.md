# TODO: Update Add New Customer Bottom Sheet

## Tasks
- [ ] Update CustomerBottomSheet.kt to make mobile number mandatory with in-field validation
- [ ] Add keyboard focus handling for name, mobile, and address fields to prevent hiding behind keyboard
- [ ] Update CustomerListScreen.kt to show success message on adding new customer with dynamic duration
- [ ] Test the changes for validation, success message, and keyboard behavior

## Details
- Make mobile number field mandatory in CustomerBottomSheet.kt
- Implement in-field validation for mobile number
- Add Modifier.imePadding() to fields for keyboard handling
- Show snackbar success message in CustomerListScreen.kt for addCustomerUiState success
- Adjust snackbar duration based on process (e.g., longer for slower processes)

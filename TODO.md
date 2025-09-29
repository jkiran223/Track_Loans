# Update Loan Card Dashboard Task

## Overview
Update the loan card from dashboard to show real-time metrics: total active loans, due loans today, paid loans today, due amount today, collection today, and a progress bar for collection vs due amount. Ensure card refreshes on transaction actions.

## Steps
- [ ] Update DashboardViewModel.kt: Add new StateFlows for dueLoansToday, paidLoansToday, dueAmountToday, collectionToday. Compute values in collect blocks using LocalDate.now() for today's date. Inject GetNextDueEmiUseCase for due calculations.
- [ ] Update LoanCard.kt: Change parameters to dueLoans, paidLoans, dueAmount, collection. Remove old sub-stats and BarChart. Add new 4 sub-stats and replace with ProgressBar for collection vs due.
- [ ] Update DashboardScreen.kt: Collect new ViewModel states and pass to LoanCard. Remove placeholders.
- [ ] Test: Compile, run app, verify metrics update live after transactions.
- [ ] Add unit tests for new ViewModel computations if needed.

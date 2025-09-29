# TODO: Update Customer Card in Dashboard

## Overview
Update the CustomerCard to show only:
1. Total customers
2. Active customers (customers with active loans)
3. Linear progress bar showing active vs total customers ratio

Ensure data is real-time via existing reactive flows in DashboardViewModel.

## Steps

### 1. Update DashboardViewModel.kt
- [ ] Add a new StateFlow for `activeCustomers`: Compute as the number of unique customerIds from active loans (LoanStatus.ACTIVE).
- [ ] Place the computation in the existing loan observation coroutine.
- [ ] Expose `activeCustomers: StateFlow<Int>` publicly.

### 2. Update DashboardScreen.kt
- [ ] Collect `activeCustomers` from the ViewModel using `collectAsState()`.
- [ ] Remove the approximation calculation for activeCustomers.
- [ ] Update CustomerCard calls to pass only `totalCustomers` and `activeCustomers` (remove `dueTodayCustomers` and `overdueCustomers`).
- [ ] Ensure no breaking changes to layout (mobile/tablet).

### 3. Update CustomerCard.kt
- [ ] Update function signature: Remove `dueTodayCustomers` and `overdueCustomers` parameters.
- [ ] Remove: Sub-stats Row (StatBadge for Active/Due/Overdue), DonutChart Row, and ChartLegendItem composables/calls.
- [ ] Keep: Header (icon + "Customers" title), main totalCustomers Text (displayMedium).
- [ ] Add: Below total, display "Active: ${activeCustomers}" using a simple Text or styled badge (e.g., titleLarge, primary color).
- [ ] Add: Below active text, a LinearProgressIndicator with progress = if (totalCustomers > 0) (activeCustomers.toFloat() / totalCustomers) else 0f.
  - Style: Use MaterialTheme colors (primary for track, primaryContainer for indicator).
  - Add modifier: .fillMaxWidth(), height 8.dp.
  - Optional: Add percentage Text below (e.g., "${progress * 100}% Active").
- [ ] Import LinearProgressIndicator if needed.
- [ ] Handle edge case: If totalCustomers == 0, show 0% progress and disable/enable appropriately.
- [ ] Retain: Clickable modifier, shadow, padding, Card structure.

### 4. Testing and Verification
- [ ] After all updates: Rebuild the project.
- [ ] Test in app: Navigate to Dashboard, verify card shows correct counts and progress bar.
- [ ] Perform customer/loan actions (add/update/delete) via CustomerListScreen/Loan screens → confirm real-time refresh.
- [ ] Check on different screen sizes (mobile/tablet).
- [ ] Verify no crashes (e.g., divide by zero).

### 5. Completion
- [ ] Mark all steps complete and use attempt_completion.

package com.trackloan.ui.component.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackloan.ui.viewmodel.PaymentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    loanId: Long,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val nextEmi by viewModel.nextEmi.collectAsState()
    val paymentForm by viewModel.paymentForm.collectAsState()
    val showConfirmation by viewModel.showConfirmation.collectAsState()
    val isExpanded by viewModel.isExpanded.collectAsState()



    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is com.trackloan.common.UiState.Success -> {
                // Dismiss bottom sheet immediately for quick close
                scope.launch {
                    sheetState.hide()
                }
                onPaymentSuccess()
                onDismiss()
            }
            else -> {}
        }
    }

    // Load next EMI when sheet opens
    LaunchedEffect(loanId) {
        viewModel.loadNextEmi(loanId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Text(
                text = "Make Payment",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Main Content
            if (!isExpanded) {
                // Collapsed State - Quick Pay and Pay Now buttons
                QuickPaySection(
                    nextEmi = nextEmi,
                    onQuickPayClick = { viewModel.showQuickPayConfirmation() },
                    onPayNowClick = { viewModel.expandSheet() }
                )
            } else {
                // Expanded State - Payment Form
                PaymentFormSection(
                    paymentForm = paymentForm,
                    nextEmi = nextEmi,
                    onAmountChange = { viewModel.updatePaymentAmount(it) },
                    onDateChange = { viewModel.updatePaymentDate(it) },
                    onShowDatePicker = { viewModel.showDatePicker() },
                    onPayClick = { viewModel.showPaymentConfirmation() },
                    onCollapse = { viewModel.collapseSheet() }
                )
            }

            // Loading indicator
            if (uiState is com.trackloan.common.UiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Date Picker Dialog
        if (viewModel.showDatePickerDialog.value) {
            DatePickerDialog(
                onDismissRequest = { viewModel.dismissDatePicker() },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDateSelection() }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDatePicker() }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = viewModel.datePickerState,
                    showModeToggle = false
                )
            }
        }

        // Confirmation Dialog
        if (showConfirmation) {
            PaymentConfirmationDialog(
                paymentForm = paymentForm,
                nextEmi = nextEmi,
                isQuickPay = !isExpanded,
                onConfirm = { viewModel.processPayment() },
                onDismiss = { viewModel.dismissConfirmation() }
            )
        }
    }
}

@Composable
private fun QuickPaySection(
    nextEmi: PaymentViewModel.NextEmiData?,
    onQuickPayClick: () -> Unit,
    onPayNowClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Pay Button
        OutlinedButton(
            onClick = onQuickPayClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = nextEmi != null
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = "Quick Pay",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Pay")
            }
        }

        // Pay Now Button
        Button(
            onClick = onPayNowClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = "Pay Now",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pay Now")
            }
        }

        // Next EMI Info (if available)
        nextEmi?.let { emi ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Next EMI Due",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "EMI ${emi.emiNumber}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Due: ${emi.dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "₹${emi.amount}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentFormSection(
    paymentForm: PaymentViewModel.PaymentFormData,
    nextEmi: PaymentViewModel.NextEmiData?,
    onAmountChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    onPayClick: () -> Unit,
    onCollapse: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Collapse button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    Icons.Default.ExpandLess,
                    contentDescription = "Collapse"
                )
            }
        }

        // Amount Field
        OutlinedTextField(
            value = paymentForm.amount,
            onValueChange = onAmountChange,
            label = { Text("Payment Amount *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Text("₹", style = MaterialTheme.typography.bodyLarge)
            },
            isError = paymentForm.amountError != null,
            supportingText = paymentForm.amountError?.let { { Text(it) } }
        )

        // Date Field
        OutlinedTextField(
            value = paymentForm.paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            onValueChange = {},
            label = { Text("Payment Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onShowDatePicker) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            isError = paymentForm.dateError != null,
            supportingText = paymentForm.dateError?.let { { Text(it) } }
        )

        // Pay Button
        Button(
            onClick = onPayClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = paymentForm.isValid
        ) {
            Text("Pay ₹${paymentForm.amount.ifEmpty { "0" }}")
        }

        // Next EMI suggestion
        nextEmi?.let { emi ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Suggested: EMI ${emi.emiNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${emi.amount} due on ${emi.dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentConfirmationDialog(
    paymentForm: PaymentViewModel.PaymentFormData,
    nextEmi: PaymentViewModel.NextEmiData?,
    isQuickPay: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isQuickPay) "Confirm Quick Payment" else "Confirm Payment")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isQuickPay && nextEmi != null) {
                    // Quick Pay summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "EMI ${nextEmi.emiNumber}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Due Date: ${nextEmi.dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Amount: ₹${nextEmi.amount}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // Custom payment summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Amount:", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "₹${paymentForm.amount}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Date:", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            paymentForm.paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

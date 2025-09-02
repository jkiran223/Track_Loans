package com.trackloan.ui.component.transaction

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackloan.domain.model.Transaction
import com.trackloan.ui.viewmodel.UpdateTransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTransactionBottomSheet(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onUpdateSuccess: () -> Unit,
    viewModel: UpdateTransactionViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val updateForm by viewModel.updateForm.collectAsState()
    val showConfirmation by viewModel.showConfirmation.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is com.trackloan.common.UiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Transaction updated successfully! ✅",
                    duration = SnackbarDuration.Short
                )
                onUpdateSuccess()
                onDismiss()
            }
            is com.trackloan.common.UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiState as com.trackloan.common.UiState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    // Initialize form with transaction data
    LaunchedEffect(transaction) {
        viewModel.initializeForm(transaction)
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
                text = "Update Transaction",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Transaction Info Card
            TransactionInfoCard(transaction)

            Spacer(modifier = Modifier.height(16.dp))

            // Update Form
            UpdateFormSection(
                updateForm = updateForm,
                onAmountChange = { viewModel.updateAmount(it) },
                onUpdateClick = { viewModel.showUpdateConfirmation() },
                onCancelClick = onDismiss
            )

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

        // Confirmation Dialog
        if (showConfirmation) {
            UpdateConfirmationDialog(
                transaction = transaction,
                updateForm = updateForm,
                onConfirm = { viewModel.updateTransaction() },
                onDismiss = { viewModel.dismissConfirmation() }
            )
        }
    }

    // Snackbar Host (invisible but functional)
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun TransactionInfoCard(transaction: Transaction) {
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
                text = "Transaction Details",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = transaction.transactionRef,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "EMI Payment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${transaction.amount}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = transaction.paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateFormSection(
    updateForm: UpdateTransactionViewModel.UpdateFormData,
    onAmountChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Amount Field
        OutlinedTextField(
            value = updateForm.amount,
            onValueChange = onAmountChange,
            label = { Text("EMI Amount *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Text("₹", style = MaterialTheme.typography.bodyLarge)
            },
            isError = updateForm.amountError != null,
            supportingText = updateForm.amountError?.let { { Text(it) } }
        )

        // Update Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Update will be recorded with current date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onUpdateClick,
                modifier = Modifier.weight(1f),
                enabled = updateForm.isValid
            ) {
                Text("Update")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateConfirmationDialog(
    transaction: Transaction,
    updateForm: UpdateTransactionViewModel.UpdateFormData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Transaction Update")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Transaction Summary
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
                            text = "Transaction ${transaction.transactionRef}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // EMI Number (if available)
                        Text(
                            text = "EMI Payment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Amount comparison
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Amount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${transaction.amount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Changes to",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "New Amount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${updateForm.amount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Update date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Update Date",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Warning message
                if (updateForm.amount.toDoubleOrNull() ?: 0.0 != transaction.amount) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This will modify the transaction amount permanently.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Back")
            }
        }
    )
}

package com.trackloan.ui.screen.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.domain.model.Transaction
import com.trackloan.ui.component.payment.PaymentBottomSheet
import com.trackloan.ui.component.transaction.UpdateTransactionBottomSheet
import com.trackloan.ui.viewmodel.EMIListViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMIListScreen(
    navController: NavController,
    loanId: Long,
    viewModel: EMIListViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val loanSummary by viewModel.loanSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showPaymentSheet by viewModel.showPaymentSheet.collectAsState()

    // State for update transaction
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showUpdateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(loanId) {
        viewModel.loadEMIList(loanId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Payment button
                    IconButton(onClick = { viewModel.showPaymentSheet() }) {
                        Icon(Icons.Default.Payment, contentDescription = "Make Payment")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Loan Summary Header
                item {
                    loanSummary?.let { summary ->
                        LoanSummaryHeader(summary)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // EMI List Header
                item {
                    Text(
                        text = "EMI Payments (${transactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // EMI List
                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No EMI payments found for this loan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        EMICard(
                            transaction = transaction,
                            onEditClick = {
                                selectedTransaction = transaction
                                showUpdateSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Payment Bottom Sheet
    if (showPaymentSheet) {
        PaymentBottomSheet(
            loanId = loanId,
            onDismiss = { viewModel.dismissPaymentSheet() },
            onPaymentSuccess = {
                viewModel.dismissPaymentSheet()
                viewModel.loadEMIList(loanId) // Refresh data
            }
        )
    }

    // Update Transaction Bottom Sheet
    if (showUpdateSheet && selectedTransaction != null) {
        UpdateTransactionBottomSheet(
            transaction = selectedTransaction!!,
            onDismiss = {
                showUpdateSheet = false
                selectedTransaction = null
            },
            onUpdateSuccess = {
                showUpdateSheet = false
                selectedTransaction = null
                viewModel.loadEMIList(loanId) // Refresh data
            }
        )
    }
}

@Composable
private fun LoanSummaryHeader(summary: EMIListViewModel.LoanSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Loan ${summary.loanId}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${summary.totalAmount}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Paid Amount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${summary.paidAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${summary.remainingAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (summary.remainingAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.completedEMIs}/${summary.totalEMIs} EMIs",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { summary.progressPercentage / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${summary.progressPercentage}% Complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EMICard(
    transaction: Transaction,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = transaction.transactionRef,
                        style = MaterialTheme.typography.titleSmall
                    )
                    TransactionStatusChip(transaction.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = transaction.paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Transaction",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${transaction.amount}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "EMI Payment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionStatusChip(status: com.trackloan.domain.model.TransactionStatus) {
    val (backgroundColor, contentColor) = when (status) {
        com.trackloan.domain.model.TransactionStatus.PAID -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        com.trackloan.domain.model.TransactionStatus.DUE -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        com.trackloan.domain.model.TransactionStatus.OVERDUE -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

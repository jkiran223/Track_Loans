package com.trackloan.ui.screen.loan

import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.Transaction
import com.trackloan.ui.component.payment.PaymentBottomSheet
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.viewmodel.LoanDetailViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoanDetailScreen(
    navController: NavController,
    loanId: Long,
    viewModel: LoanDetailViewModel = hiltViewModel()
) {
    val loan by viewModel.loan.collectAsState()
    val customer by viewModel.customer.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showPaymentSheet by viewModel.showPaymentSheet.collectAsState()

    LaunchedEffect(loanId) {
        viewModel.loadLoanDetails(loanId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Details") },
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
            loan?.let { loanData ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Sticky Loan Summary Card
                    stickyHeader {
                        Column {
                            LoanSummaryCard(loanData, customer, transactions)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Payment History Header
                    item {
                        Text(
                            text = "Payment History (${transactions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // All Transactions
                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No payments found for this loan",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(transactions) { transaction ->
                            TransactionCard(transaction, transactions)
                        }
                    }
                }
            } ?: run {
                // Loan not found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loan not found",
                        style = MaterialTheme.typography.bodyLarge
                    )
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
                viewModel.loadLoanDetails(loanId) // Refresh data
            }
        )
    }
}

@Composable
private fun LoanSummaryCard(
    loan: Loan,
    customer: com.trackloan.domain.model.Customer?,
    transactions: List<com.trackloan.domain.model.Transaction>
) {
    val paidCount = transactions.count { it.status == com.trackloan.domain.model.TransactionStatus.PAID }
    val progress = if (loan.emiTenure > 0) paidCount.toFloat() / loan.emiTenure.toFloat() else 0f

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
            // Customer Name and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer?.name ?: "Unknown Customer",
                    style = MaterialTheme.typography.headlineMedium
                )
                LoanStatusChip(loan.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loan Amount and EMI Amount - Made more prominent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Loan Amount",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${loan.loanAmount}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EMI Amount",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${loan.emiAmount}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tenure",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${loan.emiTenure} weeks",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start Date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = loan.emiStartDate.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress indicator
            Column {
                Text(
                    text = "Payment Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$paidCount of ${loan.emiTenure} EMIs paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoanStatusChip(status: com.trackloan.domain.model.LoanStatus) {
    val (backgroundColor, contentColor) = when (status) {
        com.trackloan.domain.model.LoanStatus.ACTIVE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        com.trackloan.domain.model.LoanStatus.CLOSED -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        com.trackloan.domain.model.LoanStatus.DEFAULTED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TransactionCard(transaction: Transaction, allTransactions: List<Transaction>) {
    // Calculate EMI number based on transaction sequence (sorted by payment date)
    val sortedTransactions = allTransactions.sortedBy { it.paymentDate }
    val emiNumber = sortedTransactions.indexOfFirst { it.id == transaction.id } + 1

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
                Text(
                    text = "EMI $emiNumber",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = transaction.paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${transaction.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TransactionStatusChip(transaction.status)
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

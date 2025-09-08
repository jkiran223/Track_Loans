package com.trackloan.ui.screen.loan

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.ui.viewmodel.LoanFilter
import com.trackloan.ui.viewmodel.CustomerFilter
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.LoanStatus
import com.trackloan.domain.model.Transaction
import com.trackloan.ui.component.payment.PaymentBottomSheet
import com.trackloan.ui.component.transaction.UpdateTransactionBottomSheet
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.theme.Green
import com.trackloan.ui.theme.Orange
import com.trackloan.ui.theme.Red
import com.trackloan.ui.viewmodel.TransactionFlowViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

// Function to launch the dialer with the given phone number
fun launchDialer(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFlowScreen(
    navController: NavController,
    viewModel: TransactionFlowViewModel = hiltViewModel()
) {
    val customers by viewModel.filteredCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showPaymentBottomSheet by viewModel.showPaymentBottomSheet.collectAsState()
    val selectedLoanForPayment by viewModel.selectedLoanForPayment.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val loanFilter by viewModel.loanFilter.collectAsState()
    val customerFilter by viewModel.customerFilter.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Sticky top sheet with search and customer details
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedCustomer == null) {
                        // Show search field when no customer is selected
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query -> viewModel.searchCustomers(query) },
                            label = { Text("Search Customers") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Show customer details with cross icon when customer is selected
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedCustomer!!.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Selection"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mobile number row
                        selectedCustomer!!.mobileNumber?.let { mobile ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = mobile,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { launchDialer(context, mobile) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = "Call",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Address row
                        selectedCustomer!!.address?.let { address ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Address",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Content area with proper padding from scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Customer list - show only if no customer selected
            if (selectedCustomer == null) {
                // Customer filter buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = customerFilter == CustomerFilter.ALL,
                        onClick = { viewModel.setCustomerFilter(CustomerFilter.ALL) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = customerFilter == CustomerFilter.DUE_TODAY,
                        onClick = { viewModel.setCustomerFilter(CustomerFilter.DUE_TODAY) },
                        label = { Text("Due Today") }
                    )
                    FilterChip(
                        selected = customerFilter == CustomerFilter.OVERDUE,
                        onClick = { viewModel.setCustomerFilter(CustomerFilter.OVERDUE) },
                        label = { Text("Overdue") }
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(customers) { customer ->
                        CustomerListItem(
                            customer = customer,
                            isSelected = false,
                            onClick = { viewModel.selectCustomer(customer) }
                        )
                    }
                }
            }

            // Loan list for selected customer - use remaining space
            if (selectedCustomer != null) {
                Text(
                    text = "Loans for ${selectedCustomer!!.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )

                // Filter buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = loanFilter == LoanFilter.ACTIVE,
                        onClick = { viewModel.setLoanFilter(LoanFilter.ACTIVE) },
                        label = { Text("Active") }
                    )
                    FilterChip(
                        selected = loanFilter == LoanFilter.CLOSED,
                        onClick = { viewModel.setLoanFilter(LoanFilter.CLOSED) },
                        label = { Text("Closed") }
                    )
                    FilterChip(
                        selected = loanFilter == LoanFilter.ALL,
                        onClick = { viewModel.setLoanFilter(LoanFilter.ALL) },
                        label = { Text("All") }
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(loans) { loan ->
                        var offsetX by remember { mutableStateOf(0f) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { change, dragAmount ->
                                        offsetX += dragAmount
                                        if (offsetX > 200f) {
                                            // Swipe right detected, navigate to EMI list
                                            navController.navigate(NavRoutes.EMIList.createRoute(loan.id))
                                            offsetX = 0f
                                        }
                                    }
                                }
                        ) {
                            val paidTransactions = viewModel.getTransactionsForLoan(loan.id).filter { it.status == com.trackloan.domain.model.TransactionStatus.PAID }
                            val paidEmiCount = paidTransactions.size
                            val totalEmiCount = loan.emiTenure
                            val progress = if (totalEmiCount > 0) paidEmiCount.toFloat() / totalEmiCount.toFloat() else 0f

                            var loanStatus by remember { mutableStateOf(com.trackloan.domain.model.TransactionStatus.PAID) }
                            var nextDueDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
                            // Update loan status and next due date whenever transactions change for live color refresh
                            LaunchedEffect(loan.id, viewModel.transactions.value) {
                                loanStatus = viewModel.getLoanStatus(loan.id)
                                // Get next due date
                                val nextDueResult = viewModel.getNextDueEmi(loan.id)
                                nextDueDate = when (nextDueResult) {
                                    is com.trackloan.common.Result.Success -> nextDueResult.data?.dueDate
                                    else -> null
                                }
                            }
                            LoanListItem(
                                loan = loan,
                                paidEmiCount = paidEmiCount,
                                totalEmiCount = totalEmiCount,
                                emiProgress = progress,
                                nextDueDate = nextDueDate,
                                onLoanDetailsClick = {
                                    navController.navigate(NavRoutes.LoanDetail.createRoute(loan.id))
                                },
                                onLoanClick = {
                                    viewModel.selectLoanForPayment(loan)
                                },
                                loanStatus = loanStatus
                            )
                        }
                    }
                }
            }
        }
    }

    // Show success snackbar when payment completes
    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            // Add a small delay to sync with payment processing time if needed
            kotlinx.coroutines.delay(300) // 300ms delay, adjust as needed
            snackbarHostState.showSnackbar(
                message = "Payment processed successfully! 🎉",
                duration = SnackbarDuration.Short
            )
            showSuccessSnackbar = false
        }
    }

    if (showPaymentBottomSheet && selectedLoanForPayment != null) {
        val loan = selectedLoanForPayment!!
        PaymentBottomSheet(
            loanId = loan.id,
            onDismiss = { viewModel.dismissPaymentBottomSheet() },
            onPaymentSuccess = {
                viewModel.dismissPaymentBottomSheet()
                // Refresh transactions and loans after payment success for live effect
                viewModel.selectCustomer(viewModel.selectedCustomer.value!!)
                showSuccessSnackbar = true
            }
        )
    }

    // Snackbar Host
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun CustomerListItem(
    customer: Customer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = if (isSelected) CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Customer Icon",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                customer.mobileNumber?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LoanListItem(
    loan: Loan,
    paidEmiCount: Int,
    totalEmiCount: Int,
    emiProgress: Float,
    onLoanDetailsClick: () -> Unit,
    onLoanClick: () -> Unit,
    loanStatus: com.trackloan.domain.model.TransactionStatus,
    nextDueDate: java.time.LocalDate? = null
) {
    val amountColor = when (loanStatus) {
        com.trackloan.domain.model.TransactionStatus.OVERDUE -> Red
        com.trackloan.domain.model.TransactionStatus.DUE -> Orange
        com.trackloan.domain.model.TransactionStatus.PAID -> Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onLoanClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress Bar on the left
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = emiProgress,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "$paidEmiCount of $totalEmiCount",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Summary in the center
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "₹${loan.loanAmount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = "EMI: ₹${loan.emiAmount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (nextDueDate != null) {
                    Text(
                        text = "Next Due: ${nextDueDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Icon-only button on the right
            IconButton(onClick = onLoanDetailsClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Loan Details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

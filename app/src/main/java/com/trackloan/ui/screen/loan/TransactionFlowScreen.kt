package com.trackloan.ui.screen.loan

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.LoanStatus
import com.trackloan.domain.model.Transaction
import com.trackloan.ui.component.payment.PaymentBottomSheet
import com.trackloan.ui.component.transaction.UpdateTransactionBottomSheet
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.viewmodel.TransactionFlowViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky top bar with customer name and summary
        selectedCustomer?.let { customer ->
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customer: ${customer.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Loans: ${loans.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Search field for customers
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query -> viewModel.searchCustomers(query) },
            label = { Text("Search Customers") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        // Customer list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(customers) { customer ->
                CustomerListItem(
                    customer = customer,
                    isSelected = selectedCustomer?.id == customer.id,
                    onClick = { viewModel.selectCustomer(customer) }
                )
            }
        }

        // Loan list for selected customer
        if (selectedCustomer != null) {
            Text(
                text = "Loans for ${selectedCustomer?.name}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(2f),
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
                        LoanListItem(
                            loan = loan,
                            onLoanDetailsClick = {
                                navController.navigate(NavRoutes.LoanDetail.createRoute(loan.id))
                            },
                            onLoanClick = {
                                viewModel.selectLoanForPayment(loan)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPaymentBottomSheet && selectedLoanForPayment != null) {
        val loan = selectedLoanForPayment!!
        PaymentBottomSheet(
            loanId = loan.id,
            onDismiss = { viewModel.dismissPaymentBottomSheet() },
            onPaymentSuccess = { viewModel.dismissPaymentBottomSheet() }
        )
    }

    // Transaction bottom sheet can be implemented similarly if needed
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
    onLoanDetailsClick: () -> Unit,
    onLoanClick: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Loan #${loan.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Amount: ₹${loan.loanAmount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "EMI: ₹${loan.emiAmount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onLoanDetailsClick) {
                Text("Loan Details")
            }
        }
    }
}

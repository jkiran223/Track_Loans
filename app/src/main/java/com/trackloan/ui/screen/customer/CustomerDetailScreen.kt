package com.trackloan.ui.screen.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.domain.model.Loan
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.viewmodel.CustomerDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    navController: NavController,
    customerId: Long,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val customer by viewModel.customer.collectAsState()
    val loans by viewModel.customerLoans.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            customer?.let { customerData ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Customer Summary Card
                    item {
                        CustomerSummaryCard(customerData)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Loans Section Header
                    item {
                        Text(
                            text = "Loans (${loans.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Loans List
                    if (loans.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No loans found for this customer",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(loans) { loan ->
                            LoanCard(
                                loan = loan,
                                onClick = {
                                    navController.navigate(NavRoutes.LoanDetail.createRoute(loan.id))
                                }
                            )
                        }
                    }
                }
            } ?: run {
                // Customer not found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Customer not found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerSummaryCard(customer: com.trackloan.domain.model.Customer) {
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
                text = customer.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            customer.mobileNumber?.let { mobile ->
                Row {
                    Text(
                        text = "Mobile: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mobile,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            customer.address?.let { address ->
                Row {
                    Text(
                        text = "Address: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

@Composable
private fun LoanCard(
    loan: Loan,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Loan ${loan.loanId}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount: ₹${loan.loanAmount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "EMI: ₹${loan.emiAmount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = loan.status.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (loan.status) {
                            com.trackloan.domain.model.LoanStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                            com.trackloan.domain.model.LoanStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
                            com.trackloan.domain.model.LoanStatus.DEFAULTED -> MaterialTheme.colorScheme.error
                        }
                    )
                    Text(
                        text = "${loan.emiTenure} months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.trackloan.ui.screen.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.domain.model.Customer
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.viewmodel.CustomerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    navController: NavController,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()

    // Handle UI state changes for snackbar messages
    LaunchedEffect(uiState) {
        when (uiState) {
            is com.trackloan.common.UiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Operation completed successfully",
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is com.trackloan.common.UiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (uiState as com.trackloan.common.UiState.Error).message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    viewModel.updateSearchQuery(query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search customers by name or mobile...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { navController.navigate(NavRoutes.LoanDisbursement.route) }) {
                    Text("Disburse Loan")
                }
                Button(onClick = { showBottomSheet = true }) {
                    Text("Add New Customer")
                }
            }

            // Customer List
            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No customers found" else "No customers match your search",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers) { customer ->
                        CustomerCard(
                            customer = customer,
                            onClick = {
                                selectedCustomer = customer
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for Customer Actions/Form
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedCustomer = null
            },
            sheetState = sheetState
        ) {
            CustomerBottomSheet(
                customer = selectedCustomer,
                onViewDetails = { customer ->
                    navController.navigate(NavRoutes.CustomerDetail.createRoute(customer.id))
                    showBottomSheet = false
                },
                onEdit = { /* Will be handled by expanding the sheet */ },
                onDelete = { customer ->
                    viewModel.deleteCustomer(customer.id)
                    showBottomSheet = false
                },
                onAddNew = {
                    selectedCustomer = null
                    // Sheet will show add form
                },
                onDismiss = {
                    showBottomSheet = false
                    selectedCustomer = null
                }
            )
        }
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            customer.mobileNumber?.let { mobile ->
                Text(
                    text = mobile,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            customer.address?.let { address ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

package com.trackloan.ui.screen.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.domain.model.Customer
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.viewmodel.LoanDisbursementViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDisbursementScreen(
    navController: NavController,
    viewModel: LoanDisbursementViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val showConfirmationDialog by viewModel.showConfirmationDialog.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle UI state changes for snackbar messages
    LaunchedEffect(uiState) {
        when (uiState) {
            is com.trackloan.common.UiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Loan disbursed successfully 🎉",
                        duration = SnackbarDuration.Short
                    )
                }
                // Navigate back after success
                delay(1000)
                navController.popBackStack()
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
                title = { Text("Loan Disbursement") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Customer Search Section
            CustomerSearchSection(
                searchQuery = searchQuery,
                searchResults = searchResults,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onCustomerSelect = { viewModel.selectCustomer(it) },
                onAddNewCustomer = {
                    // Navigate to customer list with add mode
                    navController.navigate(NavRoutes.CustomerList.route)
                }
            )

            // Sticky Customer Details (if selected)
            selectedCustomer?.let { customer ->
                CustomerDetailsStickySheet(
                    customer = customer,
                    onClose = { viewModel.clearSelectedCustomer() }
                )
            }

            // Loan Disbursement Form (only show if customer is selected)
            if (selectedCustomer != null) {
                LoanDisbursementForm(
                    viewModel = viewModel,
                    isFormValid = isFormValid,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Empty state when no customer selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search and select a customer to disburse a loan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        LoanConfirmationDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.dismissConfirmationDialog() },
            onConfirm = { viewModel.confirmLoanDisbursement() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerSearchSection(
    searchQuery: String,
    searchResults: List<Customer>,
    onSearchQueryChange: (String) -> Unit,
    onCustomerSelect: (Customer) -> Unit,
    onAddNewCustomer: () -> Unit
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                onSearchQueryChange(it)
                showSuggestions = it.isNotEmpty()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search customer by name or mobile...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        onSearchQueryChange("")
                        showSuggestions = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        // Search Suggestions
        if (showSuggestions && searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(searchResults) { customer ->
                    CustomerSuggestionItem(
                        customer = customer,
                        onClick = {
                            onCustomerSelect(customer)
                            showSuggestions = false
                        }
                    )
                }
            }
        }

        // Add New Customer Option (when no results found)
        if (showSuggestions && searchResults.isEmpty() && searchQuery.isNotEmpty()) {
            OutlinedCard(
                onClick = onAddNewCustomer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Add Customer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add New Customer",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "\"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerSuggestionItem(
    customer: Customer,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Customer",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium
                )
                customer.mobileNumber?.let { mobile ->
                    Text(
                        text = mobile,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailsStickySheet(
    customer: Customer,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    customer.mobileNumber?.let { mobile ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mobile,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    customer.address?.let { address ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanDisbursementForm(
    viewModel: LoanDisbursementViewModel,
    isFormValid: Boolean,
    modifier: Modifier = Modifier
) {
    val formData by viewModel.formData.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Loan Amount
        item {
            OutlinedTextField(
                value = formData.loanAmount,
                onValueChange = { viewModel.updateLoanAmount(it) },
                label = { Text("Loan Amount *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge)
                },
                isError = formData.loanAmountError != null,
                supportingText = formData.loanAmountError?.let { { Text(it) } }
            )
        }

        // EMI Amount
        item {
            OutlinedTextField(
                value = formData.emiAmount,
                onValueChange = { viewModel.updateEmiAmount(it) },
                label = { Text("EMI Amount *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge)
                },
                isError = formData.emiAmountError != null,
                supportingText = formData.emiAmountError?.let { { Text(it) } }
            )
        }

        // EMI Tenure
        item {
            OutlinedTextField(
                value = formData.emiTenure.toString(),
                onValueChange = { viewModel.updateEmiTenure(it) },
                label = { Text("EMI Tenure") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text("weeks", style = MaterialTheme.typography.bodyMedium)
                },
                isError = formData.emiTenureError != null,
                supportingText = formData.emiTenureError?.let { { Text(it) } }
            )
        }

        // Repayable Amount (Auto-calculated)
        item {
            OutlinedTextField(
                value = formData.repayableAmount,
                onValueChange = {},
                label = { Text("Repayable Amount") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge)
                },
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        }

        // EMI Type (Non-editable)
        item {
            OutlinedTextField(
                value = "Weekly",
                onValueChange = {},
                label = { Text("EMI Type") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        }

        // EMI Start Date
        item {
            OutlinedTextField(
                value = formData.emiStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = { Text("EMI Start Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { viewModel.showDatePicker() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                isError = formData.emiStartDateError != null,
                supportingText = formData.emiStartDateError?.let { { Text(it) } }
            )
        }

        // Disburse Loan Button
        item {
            Button(
                onClick = { viewModel.showConfirmationDialog() },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Disburse Loan")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanConfirmationDialog(
    viewModel: LoanDisbursementViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val formData by viewModel.formData.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Loan Disbursement")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedCustomer?.let { customer ->
                    Text("Customer: ${customer.name}")
                }
                Text("Loan Amount: ₹${formData.loanAmount}")
                Text("EMI Amount: ₹${formData.emiAmount}")
                Text("Tenure: ${formData.emiTenure} weeks")
                Text("Repayable Amount: ₹${formData.repayableAmount}")
                Text("EMI Start Date: ${formData.emiStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

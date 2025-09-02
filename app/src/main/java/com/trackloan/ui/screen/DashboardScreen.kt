package com.trackloan.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.domain.model.Customer
import com.trackloan.domain.model.Loan
import com.trackloan.domain.model.Transaction
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.theme.Orange
import com.trackloan.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val customers by viewModel.customers.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val totalCustomers by viewModel.totalCustomers.collectAsState()
    val totalActiveLoans by viewModel.totalActiveLoans.collectAsState()
    val totalClosedLoans by viewModel.totalClosedLoans.collectAsState()
    val totalPendingApprovals by viewModel.totalPendingApprovals.collectAsState()
    val emiDueToday by viewModel.emiDueToday.collectAsState()
    val totalPaid by viewModel.totalPaid.collectAsState()
    val pendingPayments by viewModel.pendingPayments.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                title = { Text("TrackLoan Dashboard") },
                actions = {
                    IconButton(onClick = { /* Global search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Details Module
            item {
                CustomerDetailsModule(
                    totalCustomers = totalCustomers,
                    customers = customers.take(5), // Show top 5 customers
                    onViewAll = { navController.navigate(NavRoutes.CustomerList.route) },
                    onAddCustomer = { /* Navigate to add customer */ },
                    onCustomerClick = { customer ->
                        navController.navigate(NavRoutes.CustomerDetail.createRoute(customer.id))
                    }
                )
            }

            // Loan Details Module
            item {
                LoanDetailsModule(
                    totalActiveLoans = totalActiveLoans,
                    totalClosedLoans = totalClosedLoans,
                    totalPendingApprovals = totalPendingApprovals,
                    loans = loans.take(5), // Show top 5 loans
                    onViewAll = { /* Navigate to loan list */ },
                    onDisburseLoan = { navController.navigate(NavRoutes.LoanDisbursement.route) },
                    onLoanClick = { loan ->
                        navController.navigate(NavRoutes.LoanDetail.createRoute(loan.id))
                    }
                )
            }

            // Transaction Details Module
            item {
                TransactionDetailsModule(
                    emiDueToday = emiDueToday,
                    totalPaid = totalPaid,
                    pendingPayments = pendingPayments,
                    transactions = transactions.take(5), // Show top 5 transactions
                    onViewAll = { /* Navigate to transaction list */ },
                    onPayEMI = { /* Navigate to pay EMI */ },
                    onTransactionClick = { transaction ->
                        /* Handle transaction click */
                    }
                )
            }

            // Reports Module
            item {
                ReportsModule(
                    onViewReports = { /* Navigate to reports */ }
                )
            }
        }
    }
}

@Composable
private fun CustomerDetailsModule(
    totalCustomers: Int,
    customers: List<Customer>,
    onViewAll: () -> Unit,
    onAddCustomer: () -> Unit,
    onCustomerClick: (Customer) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer Details",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = onAddCustomer) {
                        Icon(Icons.Default.Add, contentDescription = "Add Customer")
                    }
                    TextButton(onClick = onViewAll) {
                        Text("View All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Customers Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Total Customers: $totalCustomers",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer List
            Text(
                text = "Recent Customers",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            customers.forEach { customer ->
                CustomerCard(
                    customer = customer,
                    onClick = { onCustomerClick(customer) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LoanDetailsModule(
    totalActiveLoans: Int,
    totalClosedLoans: Int,
    totalPendingApprovals: Int,
    loans: List<Loan>,
    onViewAll: () -> Unit,
    onDisburseLoan: () -> Unit,
    onLoanClick: (Loan) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Loan Details",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    Button(onClick = onDisburseLoan) {
                        Text("Disburse Loan")
                    }
                    TextButton(onClick = onViewAll) {
                        Text("View All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Active Loans",
                    value = totalActiveLoans.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color.Green,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Closed Loans",
                    value = totalClosedLoans.toString(),
                    icon = Icons.Default.Close,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Pending Approvals",
                    value = totalPendingApprovals.toString(),
                    icon = Icons.Default.Schedule,
                    color = Orange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loan List
            Text(
                text = "Recent Loans",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            loans.forEach { loan ->
                LoanCard(
                    loan = loan,
                    onClick = { onLoanClick(loan) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TransactionDetailsModule(
    emiDueToday: Int,
    totalPaid: Int,
    pendingPayments: Int,
    transactions: List<Transaction>,
    onViewAll: () -> Unit,
    onPayEMI: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    Button(onClick = onPayEMI) {
                        Text("Pay EMI")
                    }
                    TextButton(onClick = onViewAll) {
                        Text("View All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "EMI Due Today",
                    value = emiDueToday.toString(),
                    icon = Icons.Default.DateRange,
                    color = Color.Red
                )
                SummaryCard(
                    title = "Total Paid",
                    value = totalPaid.toString(),
                    icon = Icons.Default.Check,
                    color = Color.Green
                )
                SummaryCard(
                    title = "Pending",
                    value = pendingPayments.toString(),
                    icon = Icons.Default.PendingActions,
                    color = Orange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction List
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            transactions.forEach { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReportsModule(
    onViewReports: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reports",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = onViewReports) {
                    Text("View Reports")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Placeholder for reports content
            Text(
                text = "Profit & Loss, Customer Stats, Loan Stats, Track Today",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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

@Composable
private fun LoanCard(
    loan: Loan,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
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
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Transaction #${transaction.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Amount: ₹${transaction.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Customers", Icons.Default.People, NavRoutes.CustomerList.route),
        BottomNavItem("Loans", Icons.Default.AccountBalance, NavRoutes.LoanDisbursement.route),
        BottomNavItem("Transactions", Icons.Default.Receipt, NavRoutes.TransactionFlow.route),
        BottomNavItem("Reports", Icons.Default.BarChart, "reports") // Placeholder
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = false, // TODO: Implement selection state
                onClick = {
                    when (item.route) {
                        NavRoutes.CustomerList.route -> navController.navigate(item.route)
                        NavRoutes.LoanDisbursement.route -> navController.navigate(item.route)
                        NavRoutes.TransactionFlow.route -> navController.navigate(item.route)
                        else -> {} // TODO: Implement other routes
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

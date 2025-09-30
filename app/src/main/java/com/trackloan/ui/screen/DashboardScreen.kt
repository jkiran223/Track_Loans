
package com.trackloan.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.trackloan.ui.component.dashboard.BackupCard
import com.trackloan.ui.component.dashboard.CustomerCard
import com.trackloan.ui.component.dashboard.LoanCard
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.viewmodel.BackupViewModel
import com.trackloan.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    backupViewModel: BackupViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Refresh data every time the screen is opened
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    // Collect data from ViewModel
    val totalCustomers by viewModel.totalCustomers.collectAsState()
    val activeCustomers by viewModel.activeCustomers.collectAsState()
    val totalActiveLoans by viewModel.totalActiveLoans.collectAsState()
    val totalClosedLoans by viewModel.totalClosedLoans.collectAsState()
    val totalPendingApprovals by viewModel.totalPendingApprovals.collectAsState()
    val emiDueToday by viewModel.emiDueToday.collectAsState()
    val totalPaid by viewModel.totalPaid.collectAsState()
    val pendingPayments by viewModel.pendingPayments.collectAsState()
    val dueLoansToday by viewModel.dueLoansToday.collectAsState()
    val paidLoansToday by viewModel.paidLoansToday.collectAsState()
    val dueAmountToday by viewModel.dueAmountToday.collectAsState()
    val collectionToday by viewModel.collectionToday.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val lastBackupTimestamp by backupViewModel.lastBackupTimestamp.collectAsState()

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
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is com.trackloan.common.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is com.trackloan.common.UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Failed to load dashboard data",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Button(onClick = { viewModel.refreshData() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    DashboardContent(
                        totalCustomers = totalCustomers,
                        activeCustomers = activeCustomers,
                        activeLoans = totalActiveLoans,
                        dueLoansToday = dueLoansToday,
                        paidLoansToday = paidLoansToday,
                        dueAmountToday = dueAmountToday,
                        collectionToday = collectionToday,
                        totalDuesToday = emiDueToday,
                        screenWidth = screenWidth,
                        navController = navController,
                        lastBackupTimestamp = lastBackupTimestamp
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    totalCustomers: Int,
    activeCustomers: Int,
    activeLoans: Int,
    dueLoansToday: Int,
    paidLoansToday: Int,
    dueAmountToday: Double,
    collectionToday: Double,
    totalDuesToday: Int,
    screenWidth: androidx.compose.ui.unit.Dp,
    navController: NavController,
    lastBackupTimestamp: Long?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Determine grid layout based on screen width
        val isTabletOrLarger = screenWidth >= 600.dp

                if (isTabletOrLarger) {
                    // Tablet/Desktop: 2-column grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CustomerCard(
                                totalCustomers = totalCustomers,
                                activeCustomers = activeCustomers,
                                onClick = { navController.navigate(NavRoutes.CustomerList.route) }
                            )
                            LoanCard(
                                activeLoans = activeLoans,
                                dueLoans = dueLoansToday,
                                paidLoans = paidLoansToday,
                                dueAmount = dueAmountToday,
                                collection = collectionToday,
                                onClick = { navController.navigate(NavRoutes.TransactionFlow.route) }
                            )
                            BackupCard(
                                lastBackupTimestamp = lastBackupTimestamp,
                                onBackupClick = { navController.navigate(NavRoutes.BackupRestore.route) },
                                onRestoreClick = { navController.navigate(NavRoutes.BackupRestore.route) }
                            )
                        }

                    }
                } else {
                    // Mobile: Single column
                    CustomerCard(
                        totalCustomers = totalCustomers,
                        activeCustomers = activeCustomers,
                        onClick = { navController.navigate(NavRoutes.CustomerList.route) }
                    )
                    LoanCard(
                        activeLoans = activeLoans,
                        dueLoans = dueLoansToday,
                        paidLoans = paidLoansToday,
                        dueAmount = dueAmountToday,
                        collection = collectionToday,
                        onClick = { navController.navigate(NavRoutes.TransactionFlow.route) }
                    )
                    BackupCard(
                        lastBackupTimestamp = lastBackupTimestamp,
                        onBackupClick = { navController.navigate(NavRoutes.BackupRestore.route) },
                        onRestoreClick = { navController.navigate(NavRoutes.BackupRestore.route) }
                    )

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

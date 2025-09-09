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
import com.trackloan.ui.component.dashboard.CustomerCard
import com.trackloan.ui.component.dashboard.LoanCard
import com.trackloan.ui.component.dashboard.TransactionCard
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Collect data from ViewModel
    val totalCustomers by viewModel.totalCustomers.collectAsState()
    val totalActiveLoans by viewModel.totalActiveLoans.collectAsState()
    val totalClosedLoans by viewModel.totalClosedLoans.collectAsState()
    val totalPendingApprovals by viewModel.totalPendingApprovals.collectAsState()
    val emiDueToday by viewModel.emiDueToday.collectAsState()
    val totalPaid by viewModel.totalPaid.collectAsState()
    val pendingPayments by viewModel.pendingPayments.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Calculate derived stats for cards
    val activeCustomers = totalCustomers - (totalClosedLoans + totalPendingApprovals) // Approximation
    val dueTodayCustomers = emiDueToday // Using EMI due as proxy
    val overdueCustomers = totalPendingApprovals

    val loansDisbursedToday = 5 // Placeholder - would need actual daily data
    val loansClosingToday = 3 // Placeholder - would need actual daily data

    val totalDueAmountToday = 25000f // Placeholder
    val collectedAmountToday = 18000f // Placeholder
    val pendingCollectionToday = totalDueAmountToday - collectedAmountToday
    val overdueAmount = 5000f // Placeholder

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
                        dueTodayCustomers = dueTodayCustomers,
                        overdueCustomers = overdueCustomers,
                        activeLoans = totalActiveLoans,
                        loansDisbursedToday = loansDisbursedToday,
                        loansClosingToday = loansClosingToday,
                        totalDuesToday = emiDueToday,
                        totalDueAmountToday = totalDueAmountToday,
                        collectedAmountToday = collectedAmountToday,
                        pendingCollectionToday = pendingCollectionToday,
                        overdueAmount = overdueAmount,
                        screenWidth = screenWidth,
                        navController = navController
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
    dueTodayCustomers: Int,
    overdueCustomers: Int,
    activeLoans: Int,
    loansDisbursedToday: Int,
    loansClosingToday: Int,
    totalDuesToday: Int,
    totalDueAmountToday: Float,
    collectedAmountToday: Float,
    pendingCollectionToday: Float,
    overdueAmount: Float,
    screenWidth: androidx.compose.ui.unit.Dp,
    navController: NavController
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
                                dueTodayCustomers = dueTodayCustomers,
                                overdueCustomers = overdueCustomers,
                                onClick = { navController.navigate(NavRoutes.CustomerList.route) }
                            )
                            LoanCard(
                                activeLoans = activeLoans,
                                loansDisbursedToday = loansDisbursedToday,
                                loansClosingToday = loansClosingToday,
                                onClick = { navController.navigate(NavRoutes.LoanDisbursement.route) }
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TransactionCard(
                                totalDuesToday = totalDuesToday,
                                totalDueAmountToday = totalDueAmountToday,
                                collectedAmountToday = collectedAmountToday,
                                pendingCollectionToday = pendingCollectionToday,
                                overdueAmount = overdueAmount,
                                onClick = { navController.navigate(NavRoutes.TransactionFlow.route) }
                            )
                        }
                    }
                } else {
                    // Mobile: Single column
                    CustomerCard(
                        totalCustomers = totalCustomers,
                        activeCustomers = activeCustomers,
                        dueTodayCustomers = dueTodayCustomers,
                        overdueCustomers = overdueCustomers,
                        onClick = { navController.navigate(NavRoutes.CustomerList.route) }
                    )
                    LoanCard(
                        activeLoans = activeLoans,
                        loansDisbursedToday = loansDisbursedToday,
                        loansClosingToday = loansClosingToday,
                        onClick = { navController.navigate(NavRoutes.LoanDisbursement.route) }
                    )
                    TransactionCard(
                        totalDuesToday = totalDuesToday,
                        totalDueAmountToday = totalDueAmountToday,
                        collectedAmountToday = collectedAmountToday,
                        pendingCollectionToday = pendingCollectionToday,
                        overdueAmount = overdueAmount,
                        onClick = { navController.navigate(NavRoutes.TransactionFlow.route) }
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

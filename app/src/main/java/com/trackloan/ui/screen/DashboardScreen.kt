package com.trackloan.ui.screen

import androidx.compose.foundation.layout.*
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
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TrackLoan Dashboard") },
                actions = {
                    IconButton(onClick = { /* Global search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Refresh action */ }) {
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
            // Intentionally left blank
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

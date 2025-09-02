package com.trackloan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trackloan.ui.navigation.NavRoutes
import com.trackloan.ui.screen.DashboardScreen
import com.trackloan.ui.screen.customer.CustomerDetailScreen
import com.trackloan.ui.screen.customer.CustomerListScreen
import com.trackloan.ui.screen.loan.LoanDetailScreen
import com.trackloan.ui.screen.loan.LoanDisbursementScreen
import com.trackloan.ui.screen.loan.TransactionFlowScreen
import com.trackloan.ui.theme.TrackLoanTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackLoanTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrackLoanNavHost()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackLoanNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.Dashboard.route) {
                DashboardScreen(navController = navController)
            }

            composable(NavRoutes.CustomerList.route) {
                CustomerListScreen(navController = navController)
            }

            composable(
                route = NavRoutes.CustomerDetail.route,
                arguments = listOf(
                    navArgument("customerId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
                if (customerId != -1L) {
                    CustomerDetailScreen(
                        navController = navController,
                        customerId = customerId
                    )
                }
            }

            composable(
                route = NavRoutes.LoanDetail.route,
                arguments = listOf(
                    navArgument("loanId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val loanId = backStackEntry.arguments?.getLong("loanId") ?: -1L
                LoanDetailScreen(navController = navController, loanId = loanId)
            }

            composable(NavRoutes.LoanDisbursement.route) {
                LoanDisbursementScreen(navController = navController)
            }

            composable(NavRoutes.TransactionFlow.route) {
                TransactionFlowScreen(navController = navController)
            }
        }
    }
}

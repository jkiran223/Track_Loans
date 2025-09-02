package com.trackloan.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Dashboard : NavRoutes("dashboard")
    data object CustomerList : NavRoutes("customer_list")
    data object CustomerDetail : NavRoutes("customer_detail/{customerId}") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
    data object LoanDetail : NavRoutes("loan_detail/{loanId}") {
        fun createRoute(loanId: Long) = "loan_detail/$loanId"
    }
    data object TransactionFlow : NavRoutes("transaction_flow")
    data object EMIList : NavRoutes("emi_list/{loanId}") {
        fun createRoute(loanId: Long) = "emi_list/$loanId"
    }
    data object LoanDisbursement : NavRoutes("loan_disbursement")
}

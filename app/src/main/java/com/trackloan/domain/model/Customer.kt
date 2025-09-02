package com.trackloan.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val mobileNumber: String? = null,
    val address: String? = null
)

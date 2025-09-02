package com.trackloan.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["mobileNumber"])
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobileNumber: String? = null,
    val address: String? = null
)

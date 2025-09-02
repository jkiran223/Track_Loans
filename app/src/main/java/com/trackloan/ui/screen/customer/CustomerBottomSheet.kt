package com.trackloan.ui.screen.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackloan.domain.model.Customer
import com.trackloan.ui.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBottomSheet(
    customer: Customer?,
    onViewDetails: (Customer) -> Unit,
    onEdit: (Customer) -> Unit,
    onDelete: (Customer) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    var isEditing by remember { mutableStateOf(customer == null) }
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var mobile by remember { mutableStateOf(customer?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }

    val addCustomerUiState by viewModel.addCustomerUiState.collectAsStateWithLifecycle()

    // Handle add/update customer result
    LaunchedEffect(addCustomerUiState) {
        when (addCustomerUiState) {
            is com.trackloan.common.UiState.Success -> {
                onDismiss()
            }
            is com.trackloan.common.UiState.Error -> {
                // Error is handled by the button state, no need to dismiss
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (customer != null && !isEditing) {
            // Show customer actions
            Text(
                text = customer.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedCard(
                onClick = { onViewDetails(customer) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = "View Details")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("View Details")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Edit Customer")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                onClick = {
                    // Show delete confirmation dialog
                    // For now, we'll just call onDelete
                    onDelete(customer)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Delete Customer",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            // Show add/edit form
            Text(
                text = if (customer == null) "Add New Customer" else "Edit Customer",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )

            Spacer(modifier = Modifier.height(8.dp))

            // Mobile field
            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    mobile = it
                    mobileError = null
                },
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // ✅ Correct
                isError = mobileError != null,
                supportingText = {
    if (mobileError != null) Text(mobileError!!)
}
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Address field
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        // Validate form
                        var isValid = true
                        var nameErr: String? = null
                        var mobileErr: String? = null

                        // Validate name
                        if (name.trim().isEmpty()) {
                            nameErr = "Name is required"
                            isValid = false
                        } else if (name.trim().length < 2) {
                            nameErr = "Name must be at least 2 characters"
                            isValid = false
                        } else if (name.trim().length > 30) {
                            nameErr = "Name cannot exceed 30 characters"
                            isValid = false
                        }

                        // Validate mobile (optional)
                        if (mobile.isNotEmpty()) {
                            val mobileRegex = Regex("^[+]?[0-9]{10,15}$")
                            if (!mobileRegex.matches(mobile.trim())) {
                                mobileErr = "Please enter a valid mobile number"
                                isValid = false
                            }
                        }

                        nameError = nameErr
                        mobileError = mobileErr

                        if (isValid) {
                            if (customer == null) {
                                // Add new customer
                                viewModel.addCustomer(name, mobile.takeIf { it.isNotEmpty() }, address.takeIf { it.isNotEmpty() })
                            } else {
                                // Update existing customer
                                val updatedCustomer = customer.copy(
                                    name = name,
                                    mobileNumber = mobile.takeIf { it.isNotEmpty() },
                                    address = address.takeIf { it.isNotEmpty() }
                                )
                                viewModel.updateCustomer(updatedCustomer)
                            }
                            // Dismiss will be handled by LaunchedEffect when operation completes
                        }
                    },
                    enabled = addCustomerUiState !is com.trackloan.common.UiState.Loading
                ) {
                    if (addCustomerUiState is com.trackloan.common.UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (customer == null) "Add Customer" else "Update Customer")
                    }
                }
            }
        }
    }
}

package com.example.invoicemaker_1_0_231212.model

data class Client(
    val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val address: String?,

    var isSelected: Boolean = false
)
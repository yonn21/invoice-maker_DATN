package com.example.invoicemaker_1_0_231212.model

data class Payment(
    val id: String,
    val amount: Double,
    val date: String,
    val method: String,
    val note: String?,
)
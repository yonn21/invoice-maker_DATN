package com.example.invoicemaker_1_0_231212.model

data class PaymentInstruction(
    val id: String,
    val payableTo: String,
    val method: String,
    val paymentDetail: String?,
    var isSelected: Boolean = false,
)
package com.example.invoicemaker_1_0_231212.model

data class Invoice(
    val id: String,
    val selectedClientId: String,
    val invoiceNumber: String,
    val invoiceDate: String,
    val dueDate: String,
    val total: Double,
    val status: String,

    var isFirstOfList: Boolean,
    var isLastOfList: Boolean,
    var isFirstOfMonth: Boolean,
    var isLastOfMonth: Boolean,
)
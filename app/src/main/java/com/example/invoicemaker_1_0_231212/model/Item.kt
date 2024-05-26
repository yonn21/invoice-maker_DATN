package com.example.invoicemaker_1_0_231212.model

data class Item(
    val id: String,
    val image: String?,
    val name: String,
    val description: String?,
    val stock: String?,
    val unitCost: Double,
    val unitType: String?,

    var isSelected: Boolean = false,
    var quantity: Int = 1,

    var isRemoved: Boolean = false
)
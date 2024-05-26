package com.example.invoicemaker_1_0_231212.model

data class Settings (
    var currency: String,
    var currencyPosition: String,
    var numberFormat: Int,
    var decimalPlaces: Int,
    var dateFormat: String,
    var currencySymbol: String,
)
package com.create.bills

data class InvoiceItem(
    val srNo: Int,
    val description: String,
    val hsn: String,
    val qty: Int,
    val rate: Double,
    val per:String,
    val amount: Double
)

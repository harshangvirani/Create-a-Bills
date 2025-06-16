package com.create.bills

data class DataClasses(
    val srNo: Int,
    val description: String,
    val hsn: String,
    val qty: Int,
    val rate: Double,
    val per:String,
    val amount: Double
)

data class CompanyInfo(
    val companyName: String,
    val addressLine1: String,
    val addressLine2: String,
    val gstin: String,
    val state: String,
    val code: String
)

data class BuyerInfo(
    val buyerName: String,
    val addressLine1: String,
    val addressLine2: String,
    val gstin: String,
    val state: String,
    val code: String,
    val placeOfSupply: String
)

data class BillInfo(
    val invoiceNo: String,
    val ewayBillNo: String? = null,
    val deliveryNote: String? = null,
    val paymentTerms: String? = null,
    val refeNo: String? = null,
    val refeDate: String? = null,
    val otherRefes: String? = null,
    val buyersOrderNo: String? = null,
    val dated: String? = null,
    val dispatchDoc: String? = null,
    val deliveryNoteDate: String? = null,
    val dispatchedThrough: String? = null,
    val destination: String? = null,
    val billOfLading: String? = null,
    val vehicleNo: String? = null
)
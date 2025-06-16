package com.create.bills

import android.Manifest
import android.content.Intent
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.create.bills.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestStoragePermission()
        } else {
            checkPermissions()
        }
        val companyInfo = CompanyInfo(
            companyName = "M/S ENTERPRISES",
            addressLine1 = "House No:613,Shop NO:4,CHANDERLOK",
            addressLine2 = "NEAR MANODALI ROAD,Delhi-110093",
            gstin = "27ABCDE1234F1Z5",
            state = "Maharashtra",
            code = "07"
        )

        val buyerInfo = BuyerInfo(
            buyerName = "HEMANT INTERNATIONAL",
            addressLine1 = "Plot No:4599,GIDC,Phase-3",
            addressLine2 = "Dared,Jamnagar-361004",
            gstin = "24BACPN0912P1ZP",
            state = "Gujarat",
            code = "24",
            placeOfSupply = "Gujarat"
        )

        val billInfo = BillInfo(
            invoiceNo = "1232321rc1xx",
            ewayBillNo = "odjow8451",
            deliveryNote = "",
            paymentTerms = "Online",
            refeNo = "12",
            refeDate = "12/12/12",
            otherRefes = "",
            buyersOrderNo = "REF123",
            dated = "12/12/2013",
            dispatchDoc = "DOC789",
            deliveryNoteDate = "13/06/2025",
            dispatchedThrough = "Jam Jupeter Logistics",
            destination = "Jamnagar",
            billOfLading = "dt.11-jun-25",
            vehicleNo = "MH12AB1234"
        )

        val items = listOf(
            DataClasses(1, "BRASS SCRAP", "7404", 35, 91.14, "K.G", 3189.00),
            DataClasses(2, "ALUMINIUM SCRAP", "7602", 10, 120.50, "K.G", 1205.00),
        )

        val metadataMiddle = listOf(
            "Invoice No.:" to billInfo.invoiceNo,
            "Delivery Note:" to (billInfo.deliveryNote?:""),
            "Reference No. & Date:" to ((billInfo.refeNo+" "+billInfo.refeDate)?:""),
            "Buyer's Order No.:" to (billInfo.buyersOrderNo?:""),
            "Dispatch Doc:" to (billInfo.dispatchDoc?:""),
            "Dispatched through" to (billInfo.dispatchedThrough?:""),
            "Bill of Lading/LR-RR No.:" to (billInfo.billOfLading?:""),
        )

        val metadataRight = listOf(
            "e-Way Bill No.:" to (billInfo.ewayBillNo?:""),
            "Payment Terms:" to (billInfo.paymentTerms?:""),
            "Other References:" to (billInfo.otherRefes?:""),
            "Dated:" to (billInfo.dated?:""),
            "Delivery Note Date:" to (billInfo.deliveryNoteDate?:""),
            "Destination:" to (billInfo.destination?:""),
            "Vehicle No.:" to (billInfo.vehicleNo?:""),
        )

        binding.cgstSgst.setOnClickListener {
            generateInvoicePdf(
                this,
                items,
                metadataMiddle,
                metadataRight,
                false,
                companyInfo,
                buyerInfo

            )
        }
        binding.igst.setOnClickListener {
            generateInvoicePdf(
                this, items, metadataMiddle,
                metadataRight, true, companyInfo, buyerInfo
            )
        }
    }

    private fun checkPermissions() {
        // Create a launcher to handle the permission request result
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                // Check if both permissions were granted
                val readPermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                val writePermissionGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

                if (!(readPermissionGranted && writePermissionGranted)) {
                    // If any of the permissions are denied, show a toast message
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        // Request both read and write external storage permissions
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun requestStoragePermission() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                        requestForPermissions()
                    }

                } else {
                    requestForPermissions()

                }
            }
    }

    private fun requestForPermissions() {
        var intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            addCategory("android.intent.category.DEFAULT")
            data = Uri.parse(String.format("package:%s", applicationContext.packageName))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        requestPermissionLauncher.launch(intent)
    }

}
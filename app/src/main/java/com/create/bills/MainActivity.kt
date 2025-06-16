package com.create.bills

import android.Manifest
import android.content.Intent
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

        val invoiceno = "1232321rc1xx"
        val items = listOf(
            InvoiceItem(1, "BRASS SCRAP", "7404", 35, 91.14, "K.G", 3189.00),
            InvoiceItem(2, "ALUMINIUM SCRAP", "7602", 10, 120.50, "K.G", 1205.00),
        )

        val metadataMiddle = listOf(
            "Invoice No.:" to invoiceno,
            "Delivery Note:" to "60",
            "Reference No. & Date:" to "",
            "Buyer's Order No.:" to "REF123",
            "Dispatch Doc:" to "DOC789",
            "Dispatched through" to "JAM JUPETER LOGISTICS",
            "Bill of Lading/LR-RR No.:" to "dt.11-jun-25",
        )

        val metadataRight = listOf(
            "e-Way Bill No.:" to "PO-456",
            "Payment Terms:" to "Online / Net 30",
            "Other References:" to "",
            "Dated:" to "13/06/2025",
            "Delivery Note Date:" to "13/06/2025",
            "Destination:" to "City XYZ",
            "Vehicle No.:" to "MH12AB1234",
        )

        val totalAmount = items.sumOf { it.amount }
        val cgst = totalAmount * 0.09
        val sgst = totalAmount * 0.09


        generateInvoicePdf(
            this, items, cgst, sgst, metadataMiddle,
            metadataRight
        )
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
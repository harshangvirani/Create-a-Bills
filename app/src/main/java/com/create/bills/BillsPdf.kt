package com.create.bills

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil

fun generateInvoicePdf(
    context: Context,
    items: List<DataClasses>,
    metadataMiddle: List<Pair<String, String>>,
    metadataRight: List<Pair<String, String>>,
    isIGST: Boolean,
    companyInfo: CompanyInfo,
    buyerInfo: BuyerInfo
) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    var igst = 0.0
    var cgst = 0.0
    var sgst = 0.0

    val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 9f
        style = Paint.Style.FILL
    }

    val boldTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 9f
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT,
            android.graphics.Typeface.BOLD
        )
    }


    val startX = 30f
    var currentY = 30f

    //Tax Invoice
    canvas.drawText("TAX INVOICE", 250f, currentY - 10f, textPaint)


    // Outer border
    canvas.drawRect(startX, currentY, 565f, 812f, paint)

    // Header Area - Split in Three Parts (2:1:1 ratio)
    val headerHeight = 180f
    canvas.drawRect(startX, currentY, 565f, currentY + headerHeight, paint)

    val totalHeaderWidth = 535f // 565 - 30
    val leftWidth = totalHeaderWidth * 0.5f
    val middleWidth = totalHeaderWidth * 0.25f
    val rightWidth = totalHeaderWidth * 0.25f

    val leftStartX = startX
    val middleStartX = startX + leftWidth
    val rightStartX = middleStartX + middleWidth

    // Vertical lines to separate 3 parts
    canvas.drawLine(middleStartX, currentY, middleStartX, currentY + headerHeight, paint)
    canvas.drawLine(rightStartX, currentY, rightStartX, currentY + headerHeight, paint)

    // Left Section (Company Info)
    canvas.drawText("${companyInfo.companyName}", leftStartX + 10f, currentY + 20f, boldTextPaint)
    canvas.drawText("${companyInfo.addressLine1}", leftStartX + 10f, currentY + 30f, textPaint)
    canvas.drawText("${companyInfo.addressLine2}", leftStartX + 10f, currentY + 40f, textPaint)
    canvas.drawText(
        "GSTIN/UIN: ${companyInfo.gstin}",
        leftStartX + 10f,
        currentY + 50f,
        textPaint
    )
    canvas.drawText(
        "State Name: ${companyInfo.state}, Code: ${companyInfo.code}",
        leftStartX + 10f,
        currentY + 60f,
        textPaint
    )

    // Divider between company and buyer info
    canvas.drawLine(
        leftStartX,
        currentY + 70f,
        leftStartX + leftWidth,
        currentY + 70f,
        paint
    )
    // Left Section ( Info)
    canvas.drawText("Buyer(BILL TO):", leftStartX + 10f, currentY + 90f, textPaint)
    canvas.drawText("${buyerInfo.buyerName}", leftStartX + 10f, currentY + 100f, boldTextPaint)
    canvas.drawText("${buyerInfo.addressLine1}", leftStartX + 10f, currentY + 110f, textPaint)
    canvas.drawText("${buyerInfo.addressLine2}", leftStartX + 10f, currentY + 120f, textPaint)
    canvas.drawText("GSTIN/UIN: ${buyerInfo.gstin}", leftStartX + 10f, currentY + 130f, textPaint)
    canvas.drawText(
        "State Name: ${buyerInfo.state}, Code: ${buyerInfo.code}",
        leftStartX + 10f,
        currentY + 140f,
        textPaint
    )
    canvas.drawText(
        "Place of Supply: ${buyerInfo.placeOfSupply}",
        leftStartX + 10f,
        currentY + 150f,
        textPaint
    )


    // Metadata split into middle and right sections
    var lineYMiddle = currentY + 15f
    var lineYRight = currentY + 15f

    // Draw Middle Metadata (Value below label)
    for ((label, value) in metadataMiddle) {
        canvas.drawText(label, middleStartX + 5f, lineYMiddle, textPaint)
        canvas.drawText(value, middleStartX + 5f, lineYMiddle + 10f, textPaint)
        canvas.drawLine(
            middleStartX,
            lineYMiddle + 15f,
            middleStartX + middleWidth + 2f,
            lineYMiddle + 15f,
            paint
        )
        lineYMiddle += 25f
    }

    // Draw Right Metadata (Value below label)
    for ((label, value) in metadataRight) {
        canvas.drawText(label, rightStartX + 5f, lineYRight, textPaint)
        canvas.drawText(value, rightStartX + 5f, lineYRight + 10f, textPaint)
        canvas.drawLine(
            rightStartX,
            lineYRight + 15f,
            rightStartX + rightWidth + 2f,
            lineYRight + 15f,
            paint
        )
        lineYRight += 25f
    }

    // Advance Y to below header
    currentY += headerHeight

    //currentY += 60f
    val tableWidth = 535f  // 565 - 30 (startX) => full width for content
    val columnWeights = floatArrayOf(1f, 4f, 2f, 1.5f, 2f, 1.2f, 2.3f) // Adjust proportions here
    val totalWeight = columnWeights.sum()
    val columnWidths = columnWeights.map { (it / totalWeight) * tableWidth }
    val columnsX = FloatArray(columnWidths.size + 1)
    columnsX[0] = startX
    for (i in columnWidths.indices) {
        columnsX[i + 1] = columnsX[i] + columnWidths[i]
    }

    // Header Row
    val headerTitles =
        listOf("SI No", "Description of Goods", "HSN/SAC", "Quantity", "Rate", "per", "Amount")
    for (i in headerTitles.indices) {
        canvas.drawText(headerTitles[i], columnsX[i] + 5f, currentY + 15f, textPaint)
    }

    // Line between header and item rows
    canvas.drawLine(columnsX[0], currentY + 20f, columnsX.last(), currentY + 20f, paint)

    // Determine minimum rows and calculate table height
    val minRowCount = 21
    val actualRowCount = items.size
    val rowHeight = 20f
    val totalRowCount = maxOf(minRowCount, actualRowCount)
    val tableContentHeight = 35f + rowHeight * totalRowCount  // 35f includes header padding

    // Header borders
    for (x in columnsX) {
        canvas.drawLine(x, currentY, x, currentY + tableContentHeight + 14f, paint)
    }
    canvas.drawLine(columnsX[0], currentY, columnsX.last(), currentY, paint) // top line

    // Draw rows
    var yOffset = currentY + if (isIGST) 37f else 35f
    var totalAmount = 0.00
    var qtyTotal = 0.00
    var serialNumber = 1
    for (item in items) {
        canvas.drawText(serialNumber.toString(), columnsX[0] + 5f, yOffset, textPaint)
        canvas.drawText(item.description, columnsX[1] + 5f, yOffset, textPaint)
        canvas.drawText(item.hsn, columnsX[2] + 5f, yOffset, textPaint)
        canvas.drawText(item.qty.toString() + " K.G", columnsX[3] + 5f, yOffset, textPaint)
        canvas.drawText(String.format("%.2f", item.rate), columnsX[4] + 5f, yOffset, textPaint)
        canvas.drawText(item.per, columnsX[5] + 5f, yOffset, textPaint)
        canvas.drawText(String.format("%.2f", item.amount), columnsX[6] + 5f, yOffset, textPaint)
        yOffset += rowHeight
        qtyTotal += item.qty
        totalAmount += item.amount
        serialNumber++
    }

    // Draw empty rows if actual < minimum
    repeat(minRowCount - actualRowCount) {
        for (i in 0 until columnsX.size - 1) {
            canvas.drawText("", columnsX[i] + 5f, yOffset, textPaint)
        }
        yOffset += rowHeight
    }

    // Bottom border of table
    canvas.drawLine(columnsX[0], yOffset - 10f, columnsX.last(), yOffset - 10f, paint)

    //Calculate Tax
    if (isIGST) {
        igst = totalAmount * 0.18
    } else {
        cgst = totalAmount * 0.09
        sgst = totalAmount * 0.09
    }


    // Total Amount
    var finalAmount = totalAmount + cgst + sgst + igst


    // Move down a bit after last item row
    yOffset += 10f

    // Align under the last column (Amount column)
    val amountX = columnsX[6] + 5f

    // Total Row inside Table (Qty and Amount)
    canvas.drawText("Total", columnsX[0] + 5f, yOffset, boldTextPaint)
    canvas.drawText(qtyTotal.toString() + " K.G", columnsX[3] + 5f, yOffset, boldTextPaint)
    canvas.drawText("₹${String.format("%.2f", totalAmount)}", columnsX[6] + 5f, yOffset, boldTextPaint)
    canvas.drawLine(columnsX[0], yOffset + 3f, columnsX.last(), yOffset + 3f, paint)
    yOffset += 15f

    if (isIGST) {
        // IGST
        canvas.drawText("IGST", columnsX[0] + 5f, yOffset, textPaint)
        canvas.drawText(" 18% ", columnsX[2] + 5f, yOffset, textPaint)
        canvas.drawText("₹${String.format("%.2f", igst)}", amountX, yOffset, textPaint)
        canvas.drawLine(columnsX[0], yOffset + 3f, columnsX.last(), yOffset + 3f, paint)
        yOffset += 15f

    } else {
        // CGST
        canvas.drawText("CGST", columnsX[0] + 5f, yOffset, textPaint)
        canvas.drawText(" 9% ", columnsX[2] + 5f, yOffset, textPaint)
        canvas.drawText("₹${String.format("%.2f", cgst)}", amountX, yOffset, textPaint)
        canvas.drawLine(columnsX[0], yOffset + 3f, columnsX.last(), yOffset + 3f, paint)
        yOffset += 15f

        // SGST
        canvas.drawText("SGST", columnsX[0] + 5f, yOffset, textPaint)
        canvas.drawText(" 9% ", columnsX[2] + 5f, yOffset, textPaint)
        canvas.drawText("₹${String.format("%.2f", sgst)}", amountX, yOffset, textPaint)
        canvas.drawLine(columnsX[0], yOffset + 3f, columnsX.last(), yOffset + 3f, paint)
        yOffset += 15f
    }

    //Round up amount
    val needsRoundingUp = finalAmount != ceil(finalAmount)
    if (needsRoundingUp) {
        val roundedUp = BigDecimal(finalAmount).setScale(0, RoundingMode.CEILING)
            .setScale(2, RoundingMode.UNNECESSARY)
        val showRoundUpamount = roundedUp.toDouble() - finalAmount.toDouble()
        finalAmount = roundedUp.toDouble()
        canvas.drawText("Round Off", columnsX[0] + 5f, yOffset, textPaint)
        canvas.drawText("₹${String.format("%.2f", showRoundUpamount)}", amountX, yOffset, textPaint)
        canvas.drawLine(columnsX[0], yOffset + 3f, columnsX.last(), yOffset + 3f, paint)
        yOffset += 15f
    }


    // Total Amount (Bold)
    canvas.drawText("Total Amount:", columnsX[0] + 5f, yOffset, boldTextPaint)
    canvas.drawText("₹${String.format("%.2f", finalAmount)}", amountX, yOffset, boldTextPaint)
    canvas.drawLine(columnsX[0], yOffset + 3f, columnsX.last(), yOffset + 3f, paint)
    yOffset += 5f

    //Show Total Amount in Word
    canvas.drawText("Amount Chargeable (in words):", columnsX[0] + 5f, yOffset + 10f, textPaint)
    yOffset += 20f
    val amountInWords = convertNumberToWords(finalAmount.toLong())
    canvas.drawText("INR $amountInWords Only", columnsX[0] + 5f, yOffset, boldTextPaint)
    yOffset += 15f

    // Full-width horizontal line (above footer section)
    val footerLineY = 765f
    canvas.drawLine(
        startX,             // Start from left margin
        footerLineY,
        565f,               // End at full page width
        footerLineY,
        paint
    )

    // "M/S ENTERPRISES" - Top-right aligned
    val companyName = companyInfo.companyName
    val enterpriseTextWidth = boldTextPaint.measureText(companyName)
    val enterpriseX = 565f - enterpriseTextWidth - 10f
    val enterpriseY = footerLineY + 12f
    canvas.drawText(companyName, enterpriseX, enterpriseY, boldTextPaint)

    // "Authorised Signatory" - Bottom-right aligned
    val authText = "Authorised Signatory"
    val authTextWidth = textPaint.measureText(authText)
    val authX = 565f - authTextWidth - 10f
    val authY = footerLineY + 40f
    canvas.drawText(authText, authX, authY, textPaint)

    pdfDocument.finishPage(page)
    val file = File(context.getExternalFilesDir(null), "Invoice.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
}
package com.create.bills

fun convertNumberToWords(number: Long): String {
    if (number == 0L) return "Zero"

    val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five",
        "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
        "Twelve", "Thirteen", "Fourteen", "Fifteen",
        "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )
    val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty",
        "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convert(n: Long): String {
        return when {
            n < 20 -> units[n.toInt()]
            n < 100 -> tens[(n / 10).toInt()] + if (n % 10 != 0L) " ${units[(n % 10).toInt()]}" else ""
            n < 1000 -> "${units[(n / 100).toInt()]} Hundred" + if (n % 100 != 0L) " ${convert(n % 100)}" else ""
            n < 100000 -> "${convert(n / 1000)} Thousand" + if (n % 1000 != 0L) " ${convert(n % 1000)}" else ""
            n < 10000000 -> "${convert(n / 100000)} Lakh" + if (n % 100000 != 0L) " ${convert(n % 100000)}" else ""
            else -> "${convert(n / 10000000)} Crore" + if (n % 10000000 != 0L) " ${convert(n % 10000000)}" else ""
        }
    }

    return convert(number).trim()
}

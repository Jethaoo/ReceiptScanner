package com.example.receiptscanner.utils

/**
 * Enhanced receipt data extraction utilities
 */

/**
 * Extracts total amount from receipt text.
 * Supports multiple formats:
 * - $12.50, $12,50, 12.50, 12,50
 * - $12.5, 12.5
 * - $1,234.56 (with thousands separator)
 */
fun extractTotal(text: String): String? {
    // Try multiple patterns in order of specificity
    val patterns = listOf(
        // Currency with $ and decimal: $12.50, $1,234.56
        Regex("""\$[\d,]+\.\d{2}"""),
        // Currency with $ and single decimal: $12.5
        Regex("""\$[\d,]+\.\d{1}"""),
        // Decimal with 2 places: 12.50, 1,234.56
        Regex("""\b[\d,]+\.\d{2}\b"""),
        // Decimal with 1 place: 12.5
        Regex("""\b[\d,]+\.\d{1}\b"""),
        // European format with comma: 12,50
        Regex("""\b[\d.]+,\d{2}\b"""),
    )
    
    return patterns
        .flatMap { it.findAll(text) }
        .mapNotNull { matchResult ->
            // Remove currency symbols and normalize
            matchResult.value
                .replace("$", "")
                .replace(",", "")
                .replace(".", "")
                .let { digits ->
                    if (digits.length >= 3) {
                        // Reconstruct as decimal: last 2 digits are cents
                        val dollars = digits.dropLast(2)
                        val cents = digits.takeLast(2)
                        "$dollars.$cents"
                    } else null
                }
        }
        .maxOrNull()
}

/**
 * Extracts date from receipt text.
 * Supports multiple formats:
 * - MM/DD/YYYY, DD/MM/YYYY
 * - YYYY-MM-DD
 * - MM-DD-YYYY, DD-MM-YYYY
 * - DD.MM.YYYY, MM.DD.YYYY
 */
fun extractDate(text: String): String? {
    val patterns = listOf(
        // MM/DD/YYYY or DD/MM/YYYY
        Regex("""\b\d{2}/\d{2}/\d{4}\b"""),
        // YYYY-MM-DD (ISO format)
        Regex("""\b\d{4}-\d{2}-\d{2}\b"""),
        // MM-DD-YYYY or DD-MM-YYYY
        Regex("""\b\d{2}-\d{2}-\d{4}\b"""),
        // DD.MM.YYYY or MM.DD.YYYY
        Regex("""\b\d{2}\.\d{2}\.\d{4}\b"""),
    )
    
    return patterns.firstNotNullOfOrNull { it.find(text)?.value }
}


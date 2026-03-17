package com.example.receiptscanner.navigation

import com.example.receiptscanner.data.ReceiptEntity

sealed class Screen {
    object Home : Screen()
    object Camera : Screen()
    data class Preview(val imagePath: String) : Screen()
    data class Result(val imagePath: String, val text: String) : Screen()
    object List : Screen()
    data class Detail(val receipt: ReceiptEntity) : Screen()
    data class Edit(val receipt: ReceiptEntity) : Screen()
    object Settings : Screen()
}


package com.example.receiptscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.receiptscanner.ui.theme.ReceiptScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var useDarkTheme by remember { mutableStateOf(systemDark) }

            ReceiptScannerTheme(darkTheme = useDarkTheme) {
                ReceiptScannerApp(
                    isDarkTheme = useDarkTheme,
                    onToggleTheme = { useDarkTheme = !useDarkTheme }
                )
            }
        }
    }
}



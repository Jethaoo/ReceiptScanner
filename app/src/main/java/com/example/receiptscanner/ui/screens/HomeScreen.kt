package com.example.receiptscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.receiptscanner.ui.components.GlassCard
import com.example.receiptscanner.ui.components.GlassTopAppBar
import com.example.receiptscanner.ui.components.BarChart
import com.example.receiptscanner.ui.components.BarItem
import com.example.receiptscanner.utils.MonthRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    receiptCount: Int,
    monthOptions: List<MonthRange>,
    selectedMonthIndex: Int,
    onSelectMonth: (Int) -> Unit,
    monthlyTrendBars: List<BarItem>,
    tagBars: List<BarItem>,
    paymentBars: List<BarItem>
) {
    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = { Text("Home") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Receipts scanned",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = receiptCount.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Month",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            monthOptions.forEachIndexed { index, month ->
                                item(key = month.label) {
                                    FilterChip(
                                        selected = index == selectedMonthIndex,
                                        onClick = { onSelectMonth(index) },
                                        label = { Text(month.label) },
                                    )
                                }
                            }
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Monthly trend",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        if (monthlyTrendBars.isEmpty() || monthlyTrendBars.all { it.value == 0 }) {
                            Text(
                                text = "No receipts yet for the selected period.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            BarChart(
                                bars = monthlyTrendBars,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Top tags",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        if (tagBars.isEmpty()) {
                            Text(
                                text = "No tag data for this month.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            BarChart(bars = tagBars, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Payment methods",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        if (paymentBars.isEmpty()) {
                            Text(
                                text = "No payment data for this month.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            BarChart(bars = paymentBars, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

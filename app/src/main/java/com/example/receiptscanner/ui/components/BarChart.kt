package com.example.receiptscanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BarItem(
    val label: String,
    val value: Int
)

/**
 * Simple dependency-free bar chart for small dashboards.
 */
@Composable
fun BarChart(
    bars: List<BarItem>,
    modifier: Modifier = Modifier,
    maxBarHeight: Dp = 120.dp,
    barWidth: Dp = 28.dp
) {
    val maxValue = bars.maxOfOrNull { it.value } ?: 0
    val effectiveMaxValue = maxValue.coerceAtLeast(1)

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            val barHeight = if (maxValue == 0) 0.dp else (bar.value.toFloat() / effectiveMaxValue.toFloat() * maxBarHeight.value).dp

            Column(
                modifier = Modifier.width(barWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .width(barWidth)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

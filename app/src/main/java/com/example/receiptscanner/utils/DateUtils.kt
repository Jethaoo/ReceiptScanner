package com.example.receiptscanner.utils

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class MonthRange(
    val label: String,
    val startAtMillis: Long,
    val endAtMillis: Long
)

fun getLastNMonthRanges(
    n: Int,
    zoneId: ZoneId = ZoneId.systemDefault()
): List<MonthRange> {
    require(n > 0) { "n must be > 0" }
    val now = LocalDate.now(zoneId)
    val currentYm = YearMonth.from(now)
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")

    val ranges = (0 until n).map { offset ->
        val ym = currentYm.minusMonths(offset.toLong())
        val start = ym.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val nextStart = ym.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = nextStart - 1L
        MonthRange(
            label = ym.format(formatter),
            startAtMillis = start,
            endAtMillis = end
        )
    }

    return ranges.reversed()
}
fun MonthRange.contains(epochMillis: Long): Boolean =
    epochMillis in startAtMillis..endAtMillis

fun formatMonthLabel(yearMonth: YearMonth, zoneId: ZoneId = ZoneId.systemDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
    return yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().let { formatter.format(Instant.from(it)) }
}

package com.hastakala.shop.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object TimeUtils {
    private val zoneId: ZoneId
        get() = ZoneId.systemDefault()

    private val compactFormatter = DateTimeFormatter.ofPattern("dd MMM")
    private val fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

    fun now(): Long = System.currentTimeMillis()

    fun startOfTodayMillis(): Long =
        LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()

    fun startOfWeekMillis(): Long =
        LocalDate.now(zoneId)
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

    fun startOfMonthMillis(): Long =
        LocalDate.now(zoneId)
            .withDayOfMonth(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

    fun startOfDaysAgo(days: Long): Long =
        LocalDate.now(zoneId)
            .minusDays(days)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

    fun datesBetween(startMillis: Long, endMillis: Long): List<LocalDate> {
        val start = Instant.ofEpochMilli(startMillis).atZone(zoneId).toLocalDate()
        val end = Instant.ofEpochMilli(endMillis).atZone(zoneId).toLocalDate()
        return generateSequence(start) { current ->
            current.plusDays(1).takeIf { !it.isAfter(end) }
        }.toList()
    }

    fun fileTimeStamp(nowMillis: Long = now()): String =
        Instant.ofEpochMilli(nowMillis).atZone(zoneId).format(fileFormatter)

    fun formatShortDate(date: LocalDate): String = date.format(compactFormatter)
}

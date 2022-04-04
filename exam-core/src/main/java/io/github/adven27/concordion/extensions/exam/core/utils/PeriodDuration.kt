package io.github.adven27.concordion.extensions.exam.core.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.TemporalAmount
import java.util.Date

fun parsePeriodFrom(v: String): Pair<Period, Duration> = v.split(",").filter { it.isNotBlank() }
    .map {
        val (p1, p2) = it.trim().split(" ")
        if (p1.toIntOrNull() != null) periodBy(Integer.parseInt(p1), p2)
        else periodBy(Integer.parseInt(p2), p1)
    }.fold(Pair(Period.ZERO, Duration.ZERO)) { a, n ->
        if (n is Period) a.first + n to a.second else a.first to a.second + (n as Duration)
    }

private fun periodBy(value: Int, type: String): TemporalAmount = when (type) {
    "d", "day", "days" -> Period.ofDays(value)
    "M", "month", "months" -> Period.ofMonths(value)
    "y", "year", "years" -> Period.ofYears(value)
    "h", "hour", "hours" -> Duration.ofHours(value.toLong())
    "m", "min", "minute", "minutes" -> Duration.ofMinutes(value.toLong())
    "s", "sec", "second", "seconds" -> Duration.ofSeconds(value.toLong())
    else -> throw UnsupportedOperationException("Unsupported period type $type")
}

fun parsePeriod(within: String): TemporalAmount {
    var i = 0
    while (i < within.length && Character.isDigit(within[i])) {
        i++
    }
    return periodBy(
        within.substring(0, i).toInt(),
        within.substring(i, within.length).trim()
    )
}

fun Date.plus(period: Pair<Period, Duration>): LocalDateTime =
    this.toLocalDateTime().plus(period.first).plus(period.second)

fun Date.minus(period: Pair<Period, Duration>): LocalDateTime =
    this.toLocalDateTime().minus(period.first).minus(period.second)

fun LocalDateTime.plus(period: Pair<Period, Duration>): LocalDateTime =
    this.plus(period.first).plus(period.second)

fun LocalDateTime.minus(period: Pair<Period, Duration>): LocalDateTime =
    this.minus(period.first).minus(period.second)

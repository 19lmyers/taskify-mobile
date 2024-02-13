package dev.chara.taskify.shared.domain.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale


actual fun LocalDateTime.formatAs(format: String): String =
    DateTimeFormatter.ofPattern(format).format(toJavaLocalDateTime())

actual fun LocalTime.formatAs(format: String): String =
    DateTimeFormatter.ofPattern(format).format(toJavaLocalTime())

actual val LocalDate.weekOfYear: Int
    get() {
        val weekOfYear = WeekFields.of(Locale.getDefault()).weekOfYear()
        return toJavaLocalDate().get(weekOfYear)
    }
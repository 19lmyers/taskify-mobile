package dev.chara.taskify.shared.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect fun LocalDateTime.formatAs(format: String): String

expect fun LocalTime.formatAs(format: String): String

expect val LocalDate.weekOfYear: Int

fun LocalDateTime.formatDefault(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val format = buildString {
        val comparison = date.compareTo(now.date)

        if (comparison == 0) {
            append("'Today' ")
        } else if (comparison == -1) {
            append("'Yesterday' ")
        } else if (comparison == 1) {
            append("'Tomorrow' ")
        } else if (now.date.weekOfYear == date.weekOfYear) {
            append("EEE ")
        } else {
            append("MMM dd ")
        }

        if (now.year != year) {
            append("yyyy ")
        }

        append("h:mm a")
    }

    return formatAs(format)
}

fun LocalTime.formatDefault() = formatAs("hh:mm a")

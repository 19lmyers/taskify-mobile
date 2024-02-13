package dev.chara.taskify.shared.domain.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitWeekOfYear
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter

actual fun LocalDateTime.formatAs(format: String): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = format

    val calendar = NSCalendar.currentCalendar()
    val date = calendar.dateFromComponents(toNSDateComponents())

    return dateFormatter.stringFromDate(
        date ?: throw IllegalStateException("Could not convert to NSDate: $this")
    )
}

actual fun LocalTime.formatAs(format: String): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = format

    val components = NSDateComponents()
    components.hour = hour.toLong()
    components.minute = minute.toLong()
    components.second = second.toLong()
    components.nanosecond = nanosecond.toLong()

    val calendar = NSCalendar.currentCalendar()
    val date = calendar.dateFromComponents(components)

    return dateFormatter.stringFromDate(
        date ?: throw IllegalStateException("Could not convert to NSDate: $this")
    )
}

actual val LocalDate.weekOfYear: Int
    get() {
        val calendar = NSCalendar.currentCalendar()
        val date = calendar.dateFromComponents(toNSDateComponents())
        return if (date != null) {
            calendar.component(NSCalendarUnitWeekOfYear, date).toInt()
        } else {
            -1
        }
    }
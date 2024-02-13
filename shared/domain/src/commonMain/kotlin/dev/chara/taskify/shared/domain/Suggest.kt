package dev.chara.taskify.shared.domain

import dev.chara.taskify.shared.domain.util.formatDefault
import dev.chara.taskify.shared.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Task.suggest() = if (status == Task.Status.InProgress) {
    Suggestion.InProgress
} else if (status != Task.Status.Complete && scheduledAt != null) {
    if (scheduledAt!!.isOverdue()) {
        Suggestion.Overdue(scheduledAt!!)
    } else {
        Suggestion.Scheduled(scheduledAt!!)
    }
} else {
    Suggestion.None
}

fun LocalDateTime.isOverdue() = this < Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

sealed class Suggestion(
    val description: String?
) {
    data object InProgress : Suggestion("In progress")
    data class Overdue(val at: LocalDateTime) : Suggestion("Overdue — ${at.formatDefault()}")
    data class Scheduled(val at: LocalDateTime) :
        Suggestion("Scheduled — ${at.formatDefault()}")

    data object None : Suggestion(null)
}
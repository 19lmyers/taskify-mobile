package dev.chara.taskify.shared.domain

import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Task

fun List<Task>.sortedWith(
    sortType: CategoryPrefs.SortType,
    sortDirection: CategoryPrefs.SortDirection = CategoryPrefs.SortDirection.Ascending
) = if (sortDirection == CategoryPrefs.SortDirection.Descending) {
    sortedWith(getComparator(sortType).reversed())
} else {
    sortedWith(getComparator(sortType))
}

private fun getComparator(sortType: CategoryPrefs.SortType): Comparator<Task> = when (sortType) {
    CategoryPrefs.SortType.Name -> compareBy {
        it.name
    }

    CategoryPrefs.SortType.Group -> compareBy({ it.group }, { it.name })

    CategoryPrefs.SortType.Schedule -> compareBy({ it.status != Task.Status.InProgress },
        { it.scheduledAt == null },
        { it.scheduledAt },
        { it.name })

    CategoryPrefs.SortType.LastModified -> compareBy {
        it.lastModifiedBy
    }
}
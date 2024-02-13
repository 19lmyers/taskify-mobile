package dev.chara.taskify.shared.ui.ext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs

val Category.Classifier?.friendlyName: String
    get() = when(this) {
        Category.Classifier.Grocery -> "Grocery"
        null -> "No grouping"
    }

val Category.Classifier?.icon: ImageVector
    get() = when(this) {
        Category.Classifier.Grocery -> Icons.Filled.ShoppingCart
        null -> Icons.Filled.GroupWork
    }

val CategoryPrefs.SortType?.friendlyName: String
    get() = when (this) {
        CategoryPrefs.SortType.Name, null -> "Name"
        CategoryPrefs.SortType.Group -> "Group"
        CategoryPrefs.SortType.Schedule -> "Schedule"
        CategoryPrefs.SortType.LastModified -> "Last modified"
    }

val CategoryPrefs.SortType?.icon: ImageVector
    get() =
        when (this) {
            CategoryPrefs.SortType.Name, null -> Icons.Filled.SortByAlpha
            CategoryPrefs.SortType.Group -> Icons.Filled.GroupWork
            CategoryPrefs.SortType.Schedule -> Icons.Filled.Schedule
            CategoryPrefs.SortType.LastModified -> Icons.Filled.EditCalendar
        }

val CategoryPrefs.SortDirection?.friendlyName: String
    get() = when (this) {
        CategoryPrefs.SortDirection.Ascending, null -> "Asc"
        CategoryPrefs.SortDirection.Descending -> "Desc"
    }


val CategoryPrefs.SortDirection?.icon: ImageVector
    get() =
        when (this) {
            CategoryPrefs.SortDirection.Ascending, null -> Icons.Filled.ArrowUpward
            CategoryPrefs.SortDirection.Descending -> Icons.Filled.ArrowDownward
        }
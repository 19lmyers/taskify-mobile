package dev.chara.taskify.shared.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

abstract class Id {
    abstract val hexString: String

    override fun toString() = hexString

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Id) return false

        return hexString == other.hexString
    }

    override fun hashCode() = hexString.hashCode()
}

interface Workspace {
    val id: Id

    val ownerId: String
    val members: List<String>

    val name: String

    val color: SeedColor?
}

interface Category {
    val id: Id
    val workspaceId: Id?

    val name: String

    val classifier: Classifier?

    val color: SeedColor?

    enum class Classifier {
        Grocery
    }
}

interface CategoryPrefs {
    val workspaceId: Id?
    val categoryId: Id?
    val userId: String

    val ordinal: Int

    val sortType: SortType
    val sortDirection: SortDirection

    val topTasksCount: Int

    enum class SortType {
        Name,
        Group,
        Schedule,
        LastModified
    }

    enum class SortDirection {
        Ascending,
        Descending
    }
}

interface Task {
    val id: Id
    val workspaceId: Id?
    val categoryId: Id?

    val name: String
    val group: String?

    val status: Status

    val scheduledAt: LocalDateTime?

    val assignedTo: List<String>

    val reminderFired: Boolean

    val lastModifiedAt: Instant?
    val lastModifiedBy: String?

    enum class Status {
        NotStarted,
        InProgress,
        Complete
    }
}

interface Profile {
    val userId: String
    val email: String
}
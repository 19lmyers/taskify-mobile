package dev.chara.taskify.shared.model

import kotlinx.datetime.LocalDateTime

interface MutableWorkspace : Workspace {
    override var ownerId: String

    override var name: String

    override var color: SeedColor?
}

interface MutableCategory : Category {
    override var name: String

    override var classifier: Category.Classifier?

    override var color: SeedColor?
}

interface MutableCategoryPrefs : CategoryPrefs {
    override var sortType: CategoryPrefs.SortType
    override var sortDirection: CategoryPrefs.SortDirection

    override var topTasksCount: Int
}

interface MutableTask : Task {
    override var name: String
    override var group: String?

    override var status: Task.Status

    override var assignedTo: List<String>

    override var reminderFired: Boolean

    override var scheduledAt: LocalDateTime?
}

interface MutableProfile : Profile


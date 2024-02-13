package dev.chara.taskify.shared.domain.use_case.data.task

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id

class DeleteAllCompletedTasksForCategoryUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(workspaceId: Id, categoryId: Id?) =
        accountManager.database.deleteAllCompletedTasksForCategory(workspaceId, categoryId)
}
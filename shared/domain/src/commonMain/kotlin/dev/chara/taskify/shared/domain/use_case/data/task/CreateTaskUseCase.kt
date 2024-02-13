package dev.chara.taskify.shared.domain.use_case.data.task

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableTask

class CreateTaskUseCase(
    private val accountManager: AccountManager
) {
    suspend operator fun invoke(workspaceId: Id, categoryId: Id?, builder: (MutableTask) -> Unit) {
            accountManager.database.createTask(workspaceId, categoryId, builder)
    }
}
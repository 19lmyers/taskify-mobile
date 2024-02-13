package dev.chara.taskify.shared.domain.use_case.data.task

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableTask

class UpdateTaskUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(taskId: Id, builder: (MutableTask) -> Unit) =
        accountManager.database.updateTask(taskId, builder)
}
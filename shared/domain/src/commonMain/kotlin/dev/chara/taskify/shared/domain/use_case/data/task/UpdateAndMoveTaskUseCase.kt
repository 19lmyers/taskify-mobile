package dev.chara.taskify.shared.domain.use_case.data.task

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableTask

class UpdateAndMoveTaskUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(taskId: Id, categoryId: Id?, builder: (MutableTask) -> Unit) {
        accountManager.database.updateTask(taskId, builder)
        accountManager.database.moveTask(taskId, categoryId)
    }
}
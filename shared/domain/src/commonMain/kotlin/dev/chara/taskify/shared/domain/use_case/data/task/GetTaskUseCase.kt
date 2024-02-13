package dev.chara.taskify.shared.domain.use_case.data.task

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id

class GetTaskUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(taskId: Id) =
        accountManager.database.getTask(taskId)
}
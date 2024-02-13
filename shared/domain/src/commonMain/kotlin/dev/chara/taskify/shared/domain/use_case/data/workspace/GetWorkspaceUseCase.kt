package dev.chara.taskify.shared.domain.use_case.data.workspace

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id

class GetWorkspaceUseCase(private val accountManager: AccountManager) {
    operator fun invoke(workspaceId: Id) = accountManager.database.getWorkspace(workspaceId)
}
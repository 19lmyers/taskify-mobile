package dev.chara.taskify.shared.domain.use_case.data.workspace

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id

class DeleteWorkspaceUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(workspaceId: Id) =
        accountManager.database.deleteWorkspace(workspaceId)
}
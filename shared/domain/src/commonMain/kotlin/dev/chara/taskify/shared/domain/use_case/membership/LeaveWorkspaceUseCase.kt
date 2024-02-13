package dev.chara.taskify.shared.domain.use_case.membership

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id

class LeaveWorkspaceUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(workspaceId: Id) =
        accountManager.database.leaveWorkspace(workspaceId)
}
package dev.chara.taskify.shared.domain.use_case.data.workspace

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableWorkspace

class UpdateWorkspaceUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(workspaceId: Id, builder: (MutableWorkspace) -> Unit) =
        accountManager.database.updateWorkspace(workspaceId, builder)
}
package dev.chara.taskify.shared.domain.use_case.data.workspace

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.datastore.Preferences
import dev.chara.taskify.shared.model.MutableWorkspace

class CreateWorkspaceUseCase(
    private val accountManager: AccountManager,
    private val preferences: Preferences
) {
    suspend operator fun invoke(builder: (MutableWorkspace) -> Unit): Boolean {
        val workspaceId = accountManager.database.createWorkspace(builder)
        if (workspaceId != null) {
            preferences.setSelectedWorkspace(workspaceId)
        }
        return workspaceId != null
    }
}
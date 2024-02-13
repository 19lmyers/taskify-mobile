package dev.chara.taskify.shared.domain.use_case

import dev.chara.taskify.shared.datastore.Preferences
import dev.chara.taskify.shared.model.Id

class SelectWorkspaceUseCase(private val preferences: Preferences) {
    suspend operator fun invoke(workspaceId: Id) =
        preferences.setSelectedWorkspace(workspaceId)
}
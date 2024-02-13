package dev.chara.taskify.shared.domain.use_case

import dev.chara.taskify.shared.datastore.Preferences

class GetSelectedWorkspaceUseCase(private val preferences: Preferences) {
    operator fun invoke() = preferences.getSelectedWorkspace()
}
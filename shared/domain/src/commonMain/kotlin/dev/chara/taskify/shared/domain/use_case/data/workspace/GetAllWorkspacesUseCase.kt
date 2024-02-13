package dev.chara.taskify.shared.domain.use_case.data.workspace

import dev.chara.taskify.shared.database.AccountManager

class GetAllWorkspacesUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke() = accountManager.database.getWorkspaces()
}
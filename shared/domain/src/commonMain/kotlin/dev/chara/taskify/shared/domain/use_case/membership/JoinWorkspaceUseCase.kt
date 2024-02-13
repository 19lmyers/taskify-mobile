package dev.chara.taskify.shared.domain.use_case.membership

import dev.chara.taskify.shared.database.AccountManager

class JoinWorkspaceUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(token: String) =
        accountManager.database.joinWorkspace(token)
}
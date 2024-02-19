package dev.chara.taskify.shared.domain.use_case.account

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.datastore.Preferences
import dev.chara.taskify.shared.domain.MessagingToken
import kotlinx.coroutines.flow.first

class SignInUseCase(
    private val accountManager: AccountManager,
    private val preferences: Preferences,
    private val token: MessagingToken
) {
    suspend operator fun invoke(email: String, password: String): Boolean {
        val result = accountManager.login(email, password)
        if (result) {
            token.get()?.let {
                accountManager.linkFcmToken(it)
            }

            val workspaces = accountManager.database.getWorkspaces().first().list
            preferences.setSelectedWorkspace(workspaces.firstOrNull()?.id)
        }
        return result
    }
}
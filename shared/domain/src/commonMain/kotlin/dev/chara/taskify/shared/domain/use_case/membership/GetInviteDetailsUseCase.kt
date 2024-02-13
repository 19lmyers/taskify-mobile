package dev.chara.taskify.shared.domain.use_case.membership

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class GetInviteDetailsUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(token: String) =
        accountManager.database.getInviteDetails(token)?.let { workspace ->
            combine(
                accountManager.database.getProfileFor(workspace.ownerId),
                accountManager.database.getProfilesFor(workspace.members)
            ) { owner, members ->
                Result(
                    workspace, owner, members.list, inviteIsValid = true
                )
            }
        } ?: flowOf(Result())


    data class Result(
        val workspace: Workspace? = null,
        val owner: Profile? = null,
        val members: List<Profile> = emptyList(),
        val inviteIsValid: Boolean = false,
    )
}
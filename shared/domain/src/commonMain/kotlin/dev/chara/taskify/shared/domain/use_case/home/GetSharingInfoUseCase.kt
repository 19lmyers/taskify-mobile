package dev.chara.taskify.shared.domain.use_case.home

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetSharingInfoUseCase(private val accountManager: AccountManager) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(workspaceId: Id) =
        accountManager.database.getWorkspace(workspaceId).flatMapLatest { workspace ->
            if (workspace != null) {
                combine(
                    accountManager.database.getProfileFor(workspace.ownerId),
                    accountManager.database.getProfilesFor(workspace.members)
                ) { owner, members ->
                    Result(
                        accountManager.getCurrentProfile(), workspace, owner, members.list
                    )
                }
            } else {
                flowOf(Result())
            }
        }

    data class Result(
        val current: Profile? = null,
        val workspace: Workspace? = null,
        val owner: Profile? = null,
        val members: List<Profile> = emptyList()
    )
}
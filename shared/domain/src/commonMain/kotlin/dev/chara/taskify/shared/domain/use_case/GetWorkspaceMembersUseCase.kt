package dev.chara.taskify.shared.domain.use_case

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetWorkspaceMembersUseCase(private val accountManager: AccountManager) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(workspaceId: Id) =
        accountManager.database.getWorkspace(workspaceId).flatMapLatest { workspace ->
            if (workspace == null) {
                flowOf(emptyList())
            } else {
                combine(
                    accountManager.database.getProfileFor(workspace.ownerId),
                    accountManager.database.getProfilesFor(workspace.members)
                ) { owner, members ->
                    mutableListOf<Profile>().apply {
                        owner?.let {
                            add(it)
                        }
                        addAll(members.list)
                    }
                }
            }
        }
}
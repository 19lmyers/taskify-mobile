package dev.chara.taskify.shared.domain.use_case.details

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.domain.use_case.GetWorkspaceMembersUseCase
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class GetTaskDetailsUseCase(
    private val accountManager: AccountManager,
    private val getWorkspaceMembersUseCase: GetWorkspaceMembersUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(workspaceId: Id, taskId: Id) =
        accountManager.database.getTask(taskId).flatMapLatest { task ->
            combine(
                accountManager.database.getWorkspace(workspaceId),
                getWorkspaceMembersUseCase(workspaceId),
                accountManager.database.getCategories(workspaceId),
                accountManager.database.getProfileFor(task?.lastModifiedBy)
            ) { workspace, members, categories, lastActor ->
                Result(workspace, members, categories.list, task, lastActor)
            }
        }


    data class Result(
        val workspace: Workspace?,
        val members: List<Profile>,
        val categories: List<Category>,
        val task: Task?,
        val lastModifiedBy: Profile?
    )
}

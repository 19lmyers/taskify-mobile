package dev.chara.taskify.shared.domain.use_case.details

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.domain.sortedWith
import dev.chara.taskify.shared.domain.use_case.GetWorkspaceMembersUseCase
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetCategoryDetailsUseCase(
    private val accountManager: AccountManager,
    private val getWorkspaceMembersUseCase: GetWorkspaceMembersUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        workspaceId: Id, categoryId: Id?
    ): Flow<Result> {
        val categoryFlow =
            categoryId?.let { accountManager.database.getCategory(it) } ?: flowOf(null)

        return categoryFlow.flatMapLatest { category ->
            combine(
                accountManager.database.getWorkspace(workspaceId),
                getWorkspaceMembersUseCase(workspaceId),
                accountManager.database.getCategoryPrefs(workspaceId, categoryId),
                accountManager.database.getTasks(workspaceId)
            ) { workspace, members, prefs, tasks ->
                val (current, completed) = tasks.list.filter { it.categoryId == categoryId }
                    .sortedWith(
                        prefs?.sortType ?: CategoryPrefs.SortType.Name,
                        prefs?.sortDirection ?: CategoryPrefs.SortDirection.Ascending
                    )
                    .partition { it.status != Task.Status.Complete }

                Result(
                    workspace,
                    members,
                    category,
                    prefs,
                    current,
                    completed
                )
            }
        }
    }

    data class Result(
        val workspace: Workspace?,
        val members: List<Profile>,
        val category: Category?,
        val prefs: CategoryPrefs?,
        val currentTasks: List<Task>,
        val completedTasks: List<Task>
    )
}
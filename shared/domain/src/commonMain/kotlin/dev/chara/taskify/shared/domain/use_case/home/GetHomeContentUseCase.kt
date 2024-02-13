package dev.chara.taskify.shared.domain.use_case.home

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.datastore.Preferences
import dev.chara.taskify.shared.domain.Suggestion
import dev.chara.taskify.shared.domain.sortedWith
import dev.chara.taskify.shared.domain.suggest
import dev.chara.taskify.shared.domain.use_case.GetWorkspaceMembersUseCase
import dev.chara.taskify.shared.domain.util.associateWithNullable
import dev.chara.taskify.shared.domain.util.combine
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetHomeContentUseCase(
    private val accountManager: AccountManager,
    private val preferences: Preferences,
    private val getWorkspaceMembersUseCase: GetWorkspaceMembersUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): Flow<Result> =
        preferences.getSelectedWorkspace().flatMapLatest { selectedId ->
            if (selectedId == null) {
                accountManager.database.getWorkspaces().map { workspaces ->
                    Result(workspaces = workspaces.list)
                }
            } else {
                combine(
                    accountManager.database.getWorkspaces(),
                    accountManager.database.getWorkspace(selectedId),
                    getWorkspaceMembersUseCase(selectedId),
                    accountManager.database.getCategories(selectedId),
                    accountManager.database.getAllCategoryPrefs(selectedId),
                    accountManager.database.getTasks(selectedId)
                ) { workspaces, selected, members, categoryChanges, categoryPrefs, taskChanges ->
                    if (selected == null) {
                        preferences.setSelectedWorkspace(workspaces.list.firstOrNull()?.id)
                        Result(workspaces = workspaces.list)
                    } else {
                        val tasks = taskChanges.list.filter {
                            it.status != Task.Status.Complete
                        }

                        val comparator = compareBy<Category>({ category ->
                            val prefs =
                                categoryPrefs.list.firstOrNull { it.categoryId == category.id }
                            prefs?.ordinal
                        }, { it.name })

                        val categories = categoryChanges.list.sortedWith(comparator)

                        val (suggested, other) = tasks.partition {
                            it.suggest() != Suggestion.None
                        }

                        Result(
                            workspaces = workspaces.list,
                            selectedWorkspace = selected,
                            members = members,
                            categories = categories,
                            suggestedTasks = suggested.sortedWith(CategoryPrefs.SortType.Schedule),
                            otherTasks = categories.associateWithNullable { category ->
                                val prefs =
                                    categoryPrefs.list.firstOrNull { it.categoryId == category?.id }

                                val first =
                                    other.filter { it.categoryId == category?.id }.sortedWith(
                                        prefs?.sortType ?: CategoryPrefs.SortType.Name,
                                        prefs?.sortDirection
                                            ?: CategoryPrefs.SortDirection.Ascending
                                    ).take(prefs?.topTasksCount ?: 5)

                                val allCount = tasks.count { it.categoryId == category?.id }

                                val suggestedCount =
                                    suggested.count { it.categoryId == category?.id }

                                CategoryEntry(
                                    tasks = first,
                                    incomplete = allCount - first.size - suggestedCount
                                )
                            },
                        )
                    }
                }
            }
        }

    data class CategoryEntry(
        val tasks: List<Task>,
        val incomplete: Int,
    )

    data class Result(
        val workspaces: List<Workspace>,
        val selectedWorkspace: Workspace? = null,
        val members: List<Profile> = emptyList(),
        val categories: List<Category> = emptyList(),
        val suggestedTasks: List<Task> = emptyList(),
        val otherTasks: Map<Category?, CategoryEntry> = emptyMap(),
    )
}


package dev.chara.taskify.shared.domain.use_case.home

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetReorderInfoUseCase(private val accountManager: AccountManager) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(workspaceId: Id) =
        accountManager.database.getWorkspace(workspaceId).flatMapLatest { workspace ->
            if (workspace != null) {
                combine(
                    accountManager.database.getCategories(workspace.id),
                    accountManager.database.getAllCategoryPrefs(workspace.id)
                ) { categoryChanges, prefsChanges ->
                    val comparator = compareBy<Category>({ category ->
                        val prefs = prefsChanges.list.firstOrNull { it.categoryId == category.id }
                        prefs?.ordinal
                    }, { it.name })

                    val categories = categoryChanges.list.sortedWith(comparator)
                    Result(workspace, categories, prefsChanges.list)
                }
            } else {
                flowOf(Result())
            }
        }

    data class Result(
        val workspace: Workspace? = null,
        val categories: List<Category> = emptyList(),
        val prefs: List<CategoryPrefs> = emptyList()
    )
}
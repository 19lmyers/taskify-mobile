package dev.chara.taskify.shared.domain.use_case.data.category

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.Id
import kotlinx.coroutines.flow.combine

class GetCategoriesForWorkspaceUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(workspaceId: Id) =
        combine(
            accountManager.database.getCategories(workspaceId),
            accountManager.database.getAllCategoryPrefs(workspaceId)
        ) { categories, categoryPrefs ->
            val comparator = compareBy<Category>({ category ->
                val prefs =
                    categoryPrefs.list.firstOrNull { it.categoryId == category.id }
                prefs?.ordinal
            }, { it.name })

            categories.list.sortedWith(comparator)
        }
}
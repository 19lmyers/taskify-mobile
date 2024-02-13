package dev.chara.taskify.shared.domain.use_case.data.category_prefs

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id

class ReorderCategoriesUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(
        workspaceId: Id,
        categoryId: Id,
        fromIndex: Int,
        toIndex: Int
    ) = accountManager.database.reorderCategoryPrefs(workspaceId, categoryId, fromIndex, toIndex)
}
package dev.chara.taskify.shared.domain.use_case.data.category_prefs

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableCategoryPrefs

class UpdateCategoryPrefsUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(
        workspaceId: Id,
        categoryId: Id?,
        builder: (MutableCategoryPrefs) -> Unit
    ) = accountManager.database.updateCategoryPrefs(workspaceId, categoryId, builder)
}
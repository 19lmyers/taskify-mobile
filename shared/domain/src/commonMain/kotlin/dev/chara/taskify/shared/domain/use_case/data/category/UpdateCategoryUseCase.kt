package dev.chara.taskify.shared.domain.use_case.data.category

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableCategory

class UpdateCategoryUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(categoryId: Id, builder: (MutableCategory) -> Unit) =
        accountManager.database.updateCategory(categoryId, builder)
}
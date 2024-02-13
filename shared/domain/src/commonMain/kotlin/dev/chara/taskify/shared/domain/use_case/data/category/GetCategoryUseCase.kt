package dev.chara.taskify.shared.domain.use_case.data.category

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.model.Id

class GetCategoryUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke(categoryId: Id) =
        accountManager.database.getCategory(categoryId)
}
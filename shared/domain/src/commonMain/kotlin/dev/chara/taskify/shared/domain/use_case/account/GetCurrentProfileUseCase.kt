package dev.chara.taskify.shared.domain.use_case.account

import dev.chara.taskify.shared.database.AccountManager

class GetCurrentProfileUseCase(private val accountManager: AccountManager) {
    operator fun invoke() = accountManager.getCurrentProfile()
}
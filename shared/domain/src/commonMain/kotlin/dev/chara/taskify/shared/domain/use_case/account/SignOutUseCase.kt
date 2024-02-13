package dev.chara.taskify.shared.domain.use_case.account

import dev.chara.taskify.shared.database.AccountManager

class SignOutUseCase(private val accountManager: AccountManager) {
    suspend operator fun invoke() = accountManager.logout()
}
package dev.chara.taskify.shared.domain.use_case.account

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.domain.MessagingToken

class SignUpUseCase(private val accountManager: AccountManager, private val token: MessagingToken) {
    suspend operator fun invoke(email: String, password: String): Boolean {
        val result = accountManager.register(email, password)
        if (result) {
            token.get()?.let {
                accountManager.linkFcmToken(it)
            }
        }
        return result
    }
}
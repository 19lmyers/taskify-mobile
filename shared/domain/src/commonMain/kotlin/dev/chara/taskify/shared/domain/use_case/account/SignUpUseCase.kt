package dev.chara.taskify.shared.domain.use_case.account

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.domain.MessagingToken

class SignUpUseCase(private val accountManager: AccountManager, private val token: MessagingToken) {
    suspend operator fun invoke(email: String, password: String): Result<Unit, String?> {
        val result = accountManager.register(email, password)
        if (result is Ok) {
            token.get()?.let {
                accountManager.linkFcmToken(it)
            }
        }
        return result
    }
}
package dev.chara.taskify.shared.domain.use_case

import okio.ByteString.Companion.toByteString

object GetProfileImageUseCase {
    operator fun invoke(email: String): String {
        val trimmed = email.trim().lowercase()
        val byteString = trimmed.encodeToByteArray().toByteString()
        val hash = byteString.md5().hex()

        return "https://www.gravatar.com/avatar/$hash.png?d=404"
    }
}
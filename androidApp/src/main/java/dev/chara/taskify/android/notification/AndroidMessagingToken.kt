package dev.chara.taskify.android.notification

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dev.chara.taskify.shared.domain.MessagingToken
import kotlinx.coroutines.tasks.await

class AndroidMessagingToken : MessagingToken {
    override suspend fun get(): String? = Firebase.messaging.token.await()
}
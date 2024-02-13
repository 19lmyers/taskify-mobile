package dev.chara.taskify.shared.domain

interface MessagingToken {
    suspend fun get(): String?
}
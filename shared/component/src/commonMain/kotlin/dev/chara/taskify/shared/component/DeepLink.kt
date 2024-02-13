package dev.chara.taskify.shared.component

sealed interface DeepLink {
    data object None : DeepLink

    data object CreateTask : DeepLink

    data class JoinWorkspace(val token: String) : DeepLink
}
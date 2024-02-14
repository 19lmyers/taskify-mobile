package dev.chara.taskify.shared.component

sealed interface DeepLink {
    data object None : DeepLink

    data object CreateTask : DeepLink

    data class ViewWorkspace(val id: String) : DeepLink

    data class ViewTask(val workspaceId: String, val taskId: String) : DeepLink

    data class JoinWorkspace(val token: String) : DeepLink
}
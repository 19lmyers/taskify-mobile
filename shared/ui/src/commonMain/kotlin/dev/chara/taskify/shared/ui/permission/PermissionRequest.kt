package dev.chara.taskify.shared.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.chara.taskify.shared.model.Permission

interface PermissionRequestState {
    val permission: Permission
    val isGranted: State<Boolean>
    val isHidden: State<Boolean>

    fun launchRequest()
    suspend fun hide()
}

@Composable
expect fun rememberPermissionRequestState(permission: Permission): PermissionRequestState
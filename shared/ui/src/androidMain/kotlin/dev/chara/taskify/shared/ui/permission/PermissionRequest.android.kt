package dev.chara.taskify.shared.ui.permission

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.chara.taskify.shared.domain.use_case.permission.GetPermissionRequestHiddenUseCase
import dev.chara.taskify.shared.domain.use_case.permission.SetPermissionRequestHiddenUseCase
import dev.chara.taskify.shared.model.Permission
import org.koin.compose.koinInject


@SuppressLint("InlinedApi")
private fun Permission.asManifest() = when (this) {
    Permission.Notifications -> Manifest.permission.POST_NOTIFICATIONS
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberPermissionRequestState(permission: Permission): PermissionRequestState {
    val getPermissionRequestHiddenUseCase: GetPermissionRequestHiddenUseCase = koinInject()
    val setPermissionRequestHiddenUseCase: SetPermissionRequestHiddenUseCase = koinInject()

    val state = rememberPermissionState(permission.asManifest())

    val isHidden = getPermissionRequestHiddenUseCase(permission).collectAsState(initial = false)

    return remember {
        object : PermissionRequestState {
            override val permission: Permission
                get() = permission

            override val isGranted: State<Boolean>
                get() = derivedStateOf { state.status.isGranted }

            override val isHidden: State<Boolean>
                get() = isHidden

            override fun launchRequest() {
                state.launchPermissionRequest()
            }

            override suspend fun hide() {
                setPermissionRequestHiddenUseCase(permission, true)
            }
        }
    }
}
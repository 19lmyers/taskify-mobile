package dev.chara.taskify.shared.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.chara.taskify.shared.domain.use_case.permission.GetPermissionRequestHiddenUseCase
import dev.chara.taskify.shared.domain.use_case.permission.SetPermissionRequestHiddenUseCase
import dev.chara.taskify.shared.model.Permission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import platform.UIKit.UIUserNotificationTypeAlert
import platform.UIKit.UIUserNotificationTypeBadge
import platform.UIKit.UIUserNotificationTypeSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNNotificationSettings
import platform.UserNotifications.UNUserNotificationCenter

private fun getNotificationSettings(): Flow<UNNotificationSettings?> {
    val center = UNUserNotificationCenter.currentNotificationCenter()

    return callbackFlow {
        center.getNotificationSettingsWithCompletionHandler {
            trySend(it)
            cancel()
        }

        awaitClose {}
    }
}

private fun requestNotificationAuthorization(): Flow<Boolean> {
    val center = UNUserNotificationCenter.currentNotificationCenter()

    return callbackFlow {
        center.requestAuthorizationWithOptions(
            UIUserNotificationTypeAlert
                    or UIUserNotificationTypeBadge
                    or UIUserNotificationTypeSound
        ) { result, _ ->
            trySend(result)
            cancel()
        }

        awaitClose {}
    }
}

@Composable
actual fun rememberPermissionRequestState(permission: Permission): PermissionRequestState {
    val coroutineScope = rememberCoroutineScope { Dispatchers.Main }

    val getPermissionRequestHiddenUseCase: GetPermissionRequestHiddenUseCase = koinInject()
    val setPermissionRequestHiddenUseCase: SetPermissionRequestHiddenUseCase = koinInject()

    val isGranted = remember { mutableStateOf(false) }

    if (permission == Permission.Notifications) {
        coroutineScope.launch {
            isGranted.value =
                getNotificationSettings().first()?.authorizationStatus == UNAuthorizationStatusAuthorized
        }
    }

    val isHidden = getPermissionRequestHiddenUseCase(permission).collectAsState(initial = false)

    fun launchRequest() {
        when (permission) {
            Permission.Notifications -> {
                coroutineScope.launch {
                    isGranted.value = requestNotificationAuthorization().first()
                }
            }
        }
    }

    return object : PermissionRequestState {
        override val permission: Permission
            get() = permission

        override val isGranted: State<Boolean>
            get() = isGranted

        override val isHidden: State<Boolean>
            get() = isHidden

        override fun launchRequest() = launchRequest()

        override suspend fun hide() {
            setPermissionRequestHiddenUseCase(permission, true)
        }
    }
}
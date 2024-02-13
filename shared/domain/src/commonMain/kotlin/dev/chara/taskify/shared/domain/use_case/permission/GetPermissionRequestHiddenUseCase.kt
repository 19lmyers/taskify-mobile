package dev.chara.taskify.shared.domain.use_case.permission

import dev.chara.taskify.shared.datastore.Preferences
import dev.chara.taskify.shared.model.Permission

class GetPermissionRequestHiddenUseCase(private val preferences: Preferences) {
    operator fun invoke(permission: Permission) = preferences.getPermissionRequestHidden(permission)
}
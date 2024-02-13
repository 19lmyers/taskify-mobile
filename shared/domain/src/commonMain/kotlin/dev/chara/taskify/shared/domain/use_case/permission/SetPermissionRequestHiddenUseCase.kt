package dev.chara.taskify.shared.domain.use_case.permission

import dev.chara.taskify.shared.datastore.Preferences
import dev.chara.taskify.shared.model.Permission

class SetPermissionRequestHiddenUseCase(private val preferences: Preferences) {
    suspend operator fun invoke(permission: Permission, isHidden: Boolean) {
        preferences.setPermissionRequestHidden(permission, isHidden)
    }
}
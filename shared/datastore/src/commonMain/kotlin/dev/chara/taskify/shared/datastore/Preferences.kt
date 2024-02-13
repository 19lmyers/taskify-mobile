package dev.chara.taskify.shared.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Permission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

expect class DataStorePath {
    fun get(fileName: String): String
}

class Preferences(private val dataStorePath: DataStorePath) {
    private val dataStore =
        PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = null,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { dataStorePath.get("taskify.preferences_pb").toPath() }
        )

    fun getSelectedWorkspace(): Flow<Id?> = dataStore.data.map {
        it[KEY_SELECTED_WORKSPACE].let { id ->
            if (!id.isNullOrBlank()) {
                CachedId(id)
            } else {
                null
            }
        }
    }

    suspend fun setSelectedWorkspace(id: Id?) {
        dataStore.edit {
            it[KEY_SELECTED_WORKSPACE] = id?.hexString ?: ""
        }
    }

    fun getPermissionRequestHidden(permission: Permission): Flow<Boolean> = dataStore.data.map {
        it[permission.asKey()] ?: false
    }

    suspend fun setPermissionRequestHidden(permission: Permission, isHidden: Boolean) {
        dataStore.edit {
            it[permission.asKey()] = isHidden
        }
    }

    companion object {
        private val KEY_SELECTED_WORKSPACE = stringPreferencesKey("current_workspace")

        private fun Permission.asKey() = when (this) {
            Permission.Notifications -> booleanPreferencesKey("permission_request_hidden_notifications")
        }
    }
}
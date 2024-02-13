package dev.chara.taskify.shared.database

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import dev.chara.taskify.shared.model.Profile
import io.realm.kotlin.Realm
import io.realm.kotlin.annotations.ExperimentalRealmSerializerApi
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.exceptions.ServiceException
import io.realm.kotlin.mongodb.ext.call
import io.realm.kotlin.mongodb.ext.customData
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.mongodb.syncSession

class AccountManager(appId: String) {
    private val app = App.create(appId)

    private var _database: RealmDatabase? = null

    val database: RealmDatabase
        get() = _database!!

    init {
        if (app.currentUser != null) {
            val result = establishRealm(app.currentUser!!)
            if (result is Err) {
                println(result.error)
                // TODO log error
            }
        }
    }

    @OptIn(ExperimentalRealmSerializerApi::class)
    fun getCurrentProfile(): Profile? {
        return app.currentUser?.customData<CurrentProfile>()
    }

    suspend fun register(email: String, password: String) = try {
        app.emailPasswordAuth.registerUser(email, password)
        login(email, password)
    } catch (ex: ServiceException) {
        Err(ex.message)
    }

    suspend fun login(email: String, password: String) = try {
        val user = app.login(Credentials.emailPassword(email, password))
        establishRealm(user)
    } catch (ex: ServiceException) {
        Err(ex.message)
    }

    suspend fun linkFcmToken(token: String) = app.currentUser?.functions?.call<Boolean>("link_fcm_token", token)

    private fun establishRealm(user: User) = try {
        val config =
            SyncConfiguration.Builder(
                user = user,
                schema = setOf(
                    RealmUserInfo::class,
                    RealmWorkspace::class,
                    RealmCategory::class,
                    RealmCategoryPrefs::class,
                    RealmTask::class
                )
            )
                .initialSubscriptions(rerunOnOpen = true) {
                    add(it.query<RealmCategoryPrefs>("userId == $0", user.id), "category-prefs")
                    add(it.query<RealmUserInfo>(), "user-info")
                }
                .build()

        val realm = Realm.open(config)
        _database = RealmDatabase(user, realm)

        Ok(Unit)
    } catch (ex: Exception) {
        Err(ex.message)
    }

    suspend fun refresh(): Boolean {
        _database?.realm?.syncSession?.downloadAllServerChanges()
        app.currentUser?.refreshCustomData()
        return true
    }

    suspend fun logout() =
        try {
            app.currentUser?.logOut()
            _database?.realm?.close()
            _database = null
            Ok(Unit)
        } catch (ex: ServiceException) {
            Err(ex.message)
        }

}
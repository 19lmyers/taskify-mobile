package dev.chara.taskify.shared.database

import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.ListChange
import dev.chara.taskify.shared.model.MutableCategory
import dev.chara.taskify.shared.model.MutableCategoryPrefs
import dev.chara.taskify.shared.model.MutableTask
import dev.chara.taskify.shared.model.MutableWorkspace
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.SeedColor
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.model.Workspace
import io.realm.kotlin.Realm
import io.realm.kotlin.annotations.ExperimentalRealmSerializerApi
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.annotations.ExperimentalFlexibleSyncApi
import io.realm.kotlin.mongodb.ext.call
import io.realm.kotlin.mongodb.ext.subscribe
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.syncSession
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.SingleQueryChange
import io.realm.kotlin.query.max
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.mongodb.kbson.BsonDocument
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.BsonString
import org.mongodb.kbson.ObjectId
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class RealmDatabase(private var user: User, internal var realm: Realm) {

    fun getProfileFor(id: String?): Flow<Profile?> =
        realm.query<RealmUserInfo>("userId == $0", id).first().asFlow()
            .map(SingleQueryChange<RealmUserInfo>::toModel)

    fun getProfilesFor(ids: List<String>): Flow<ListChange<Profile>> =
        realm.query<RealmUserInfo>("userId IN $0", ids).asFlow()
            .map(ResultsChange<RealmUserInfo>::toModel)

    @OptIn(ExperimentalFlexibleSyncApi::class)
    suspend fun getWorkspaces(): Flow<ListChange<Workspace>> =
        realm.query<RealmWorkspace>().subscribe("workspaces", updateExisting = true).asFlow()
            .map(ResultsChange<RealmWorkspace>::toModel)

    fun getWorkspace(id: Id): Flow<Workspace?> =
        realm.query<RealmWorkspace>("_id == $0", id.toObjectId()).first().asFlow()
            .map(SingleQueryChange<RealmWorkspace>::toModel)

    @OptIn(ExperimentalRealmSerializerApi::class)
    suspend fun createWorkspace(builder: (MutableWorkspace) -> Unit): Id? {
        val workspace = RealmWorkspace().apply(builder)
        val result = user.functions.call<ObjectId?>("workspace/create") {
            add(workspace, RealmWorkspace.serializer())
        }
        if (result != null) {
            user.refreshCustomData()
            realm.syncSession.downloadAllServerChanges()
        }
        return result?.toId()
    }

    suspend fun updateWorkspace(workspaceId: Id, builder: (MutableWorkspace) -> Unit) {
        val workspace =
            realm.query<RealmWorkspace>("_id == $0", workspaceId.toObjectId()).first().find()
        realm.write {
            workspace?.let {
                findLatest(it)?.apply(builder)
            }
        }
    }


    suspend fun deleteWorkspace(workspaceId: Id): Boolean {
        val result = user.functions.call<Boolean>("workspace/delete", workspaceId.toString())
        if (result) {
            user.refreshCustomData()
            realm.syncSession.downloadAllServerChanges()
            realm.subscriptions.update {
                remove("$workspaceId/categories")
                remove("$workspaceId/tasks")
            }
        }
        return result
    }

    suspend fun getInviteToken(workspaceId: Id): String? =
        user.functions.call<String?>("workspace/invite", workspaceId.toString())

    suspend fun getInviteDetails(inviteToken: String): Workspace? =
        user.functions.call<BsonDocument?>("workspace/invite_details", inviteToken)?.let {
            val id = (it["id"] as? BsonObjectId) ?: return null

            val name = (it["name"] as? BsonString)?.value ?: return null

            val colorName = (it["color"] as? BsonString)?.value
            val color = enumValueOfOrNull<SeedColor>(colorName)

            val ownerId = (it["ownerId"] as? BsonString)?.value ?: return null

            RealmWorkspace().apply {
                this._id = id
                this.name = name
                this.color = color
                this.ownerId = ownerId
            }
        }

    suspend fun joinWorkspace(inviteToken: String): Boolean {
        val result = user.functions.call<Boolean>("workspace/join", inviteToken)
        if (result) {
            user.refreshCustomData()
            realm.syncSession.downloadAllServerChanges()
        }
        return result
    }

    suspend fun leaveWorkspace(workspaceId: Id): Boolean {
        val result = user.functions.call<Boolean>("workspace/leave", workspaceId.toString())
        if (result) {
            user.refreshCustomData()
            realm.syncSession.downloadAllServerChanges()
        }
        return result
    }

    suspend fun removeMemberFromWorkspace(workspaceId: Id, userId: String): Boolean =
        user.functions.call<Boolean>("workspace/remove", workspaceId.toString(), userId)

    fun getCategory(categoryId: Id): Flow<Category?> =
        realm.query<RealmCategory>("_id == $0", categoryId.toObjectId()).first().asFlow()
            .map(SingleQueryChange<RealmCategory>::toModel)

    @OptIn(ExperimentalFlexibleSyncApi::class)
    suspend fun getCategories(workspaceId: Id): Flow<ListChange<Category>> =
        realm.query<RealmCategory>("_workspaceId == $0", workspaceId.toObjectId())
            .subscribe("$workspaceId/categories", updateExisting = true).asFlow()
            .map(ResultsChange<RealmCategory>::toModel)

    suspend fun createCategory(workspaceId: Id, builder: (MutableCategory) -> Unit): Unit =
        realm.write {
            val category = RealmCategory().apply(builder).apply {
                _workspaceId = workspaceId.toObjectId()
            }
            copyToRealm(category)
        }

    suspend fun updateCategory(categoryId: Id, builder: (MutableCategory) -> Unit) {
        val category =
            realm.query<RealmCategory>("_id == $0", categoryId.toObjectId()).first().find()
        realm.write {
            category?.let {
                findLatest(it)?.apply(builder)
            }
        }
    }

    suspend fun deleteCategory(categoryId: Id) {
        val objectId = categoryId.toObjectId()

        val tasks = realm.query<RealmTask>("_categoryId == $0", objectId).find();

        val category = realm.query<RealmCategory>("_id == $0", objectId).first().find()

        realm.write {
            for (task in tasks) {
                findLatest(task)?.let {
                    it._categoryId = null

                    it.lastModifiedAt = Clock.System.now()
                    it.lastModifiedBy = user.id
                }
            }
            category?.let { category ->
                findLatest(category)?.let {
                    delete(it)
                }
            }
        }
    }

    fun getCategoryPrefs(workspaceId: Id, categoryId: Id?): Flow<CategoryPrefs?> =
        realm.query<RealmCategoryPrefs>(
            "_workspaceId == $0 AND _categoryId == $1 AND userId == $2",
            workspaceId.toObjectId(),
            categoryId?.toObjectId(),
            user.id
        ).first().asFlow().map(SingleQueryChange<RealmCategoryPrefs>::toModel)

    fun getAllCategoryPrefs(workspaceId: Id): Flow<ListChange<CategoryPrefs>> =
        realm.query<RealmCategoryPrefs>(
            "_workspaceId == $0 AND userId == $1", workspaceId.toObjectId(), user.id
        ).asFlow().map(ResultsChange<RealmCategoryPrefs>::toModel)

    suspend fun createCategoryPrefs(
        workspaceId: Id, categoryId: Id?, builder: (MutableCategoryPrefs) -> Unit
    ) {
        val maxOrdinal = getMaxCategoryOrdinal(workspaceId) ?: -1
        println(maxOrdinal)

        realm.write {
            val prefs = RealmCategoryPrefs().apply(builder).apply {
                _workspaceId = workspaceId.toObjectId()
                _categoryId = categoryId?.toObjectId()
                userId = user.id
                ordinal = maxOrdinal + 1
            }
            copyToRealm(prefs)
        }
    }

    suspend fun updateCategoryPrefs(
        workspaceId: Id, categoryId: Id?, builder: (MutableCategoryPrefs) -> Unit
    ) {
        val prefs = realm.query<RealmCategoryPrefs>(
            "_workspaceId == $0 AND _categoryId == $1 AND userId == $2",
            workspaceId.toObjectId(),
            categoryId?.toObjectId(),
            user.id
        ).first().find()

        if (prefs == null) {
            createCategoryPrefs(workspaceId, categoryId, builder)
        } else {
            realm.write {
                findLatest(prefs)?.apply(builder)
            }
        }
    }

    suspend fun reorderCategoryPrefs(
        workspaceId: Id, categoryId: Id, fromIndex: Int, toIndex: Int
    ) {
        val workspaceObjectId = workspaceId.toObjectId()

        val lowerBound = min(fromIndex, toIndex)
        val upperBound = max(fromIndex, toIndex)
        val differenceSign = (fromIndex - toIndex).sign

        val prefsToMove = realm.query<RealmCategoryPrefs>(
            "_workspaceId == $0 AND _categoryId == $1 AND userId == $2",
            workspaceObjectId,
            categoryId.toObjectId(),
            user.id
        ).first().find()

        val prefsToReorder = realm.query<RealmCategoryPrefs>(
            "_workspaceId == $0 AND userId == $1 AND ordinal BETWEEN {$2, $3}",
            workspaceObjectId,
            user.id,
            lowerBound,
            upperBound
        ).find()

        realm.write {
            for (prefs in prefsToReorder) {
                findLatest(prefs)?.apply {
                    ordinal += differenceSign
                }
            }

            prefsToMove?.let {
                findLatest(it)?.apply {
                    ordinal = toIndex
                }
            }
        }
    }

    suspend fun deleteCategoryPrefs(
        workspaceId: Id,
        categoryId: Id?,
    ) {
        val prefs = realm.query<RealmCategoryPrefs>(
            "_workspaceId == $0 AND _categoryId == $1 AND userId == $2",
            workspaceId.toObjectId(),
            categoryId?.toObjectId(),
            user.id
        ).first().find()

        realm.write {
            prefs?.let { prefs ->
                findLatest(prefs)?.let {
                    delete(it)
                }
            }
        }
    }

    private suspend fun getMaxCategoryOrdinal(workspaceId: Id) = realm.query<RealmCategoryPrefs>(
        "_workspaceId == $0 AND userId == $1",
        workspaceId.toObjectId(),
        user.id
    ).max<Int>("ordinal")
        .find()

    fun getTask(taskId: Id): Flow<Task?> =
        realm.query<RealmTask>("_id == $0", taskId.toObjectId()).first().asFlow()
            .map(SingleQueryChange<RealmTask>::toModel)

    @OptIn(ExperimentalFlexibleSyncApi::class)
    suspend fun getTasks(workspaceId: Id): Flow<ListChange<Task>> =
        realm.query<RealmTask>("_workspaceId == $0", workspaceId.toObjectId())
            .subscribe("$workspaceId/tasks", updateExisting = true).asFlow()
            .map(ResultsChange<RealmTask>::toModel)

    suspend fun createTask(workspaceId: Id, categoryId: Id?, builder: (MutableTask) -> Unit): Unit =
        realm.write {
            println(workspaceId)
            val task = RealmTask().apply(builder).apply {
                _workspaceId = workspaceId.toObjectId()
                _categoryId = categoryId?.toObjectId()

                lastModifiedAt = Clock.System.now()
                lastModifiedBy = user.id
            }
            copyToRealm(task)
        }

    suspend fun updateTask(taskId: Id, builder: (MutableTask) -> Unit) {
        val task = realm.query<RealmTask>("_id == $0", taskId.toObjectId()).first().find();
        realm.write {
            task?.let {
                findLatest(it)?.apply(builder)?.apply {
                    lastModifiedAt = Clock.System.now()
                    lastModifiedBy = user.id
                }
            }
        }
    }

    suspend fun moveTask(taskId: Id, categoryId: Id?) {
        val task = realm.query<RealmTask>("_id == $0", taskId.toObjectId()).first().find();
        realm.write {
            task?.let {
                findLatest(it)?.apply {
                    _categoryId = categoryId?.toObjectId()

                    lastModifiedAt = Clock.System.now()
                    lastModifiedBy = user.id
                }
            }
        }
    }

    suspend fun deleteTask(taskId: Id) {
        val task = realm.query<RealmTask>("_id == $0", taskId.toObjectId()).first().find();
        realm.write {
            task?.let { task ->
                findLatest(task)?.let {
                    delete(it)
                }
            }
        }
    }

    suspend fun deleteAllCompletedTasksForCategory(workspaceId: Id, categoryId: Id?) {
        val tasks = realm.query<RealmTask>(
            "_workspaceId == $0 AND _categoryId == $1 AND _status == '${Task.Status.Complete.name}'",
            workspaceId.toObjectId(),
            categoryId?.toObjectId()
        ).find()

        realm.write {
            for (task in tasks) {
                findLatest(task)?.let { delete(it) }
            }
        }
    }
}
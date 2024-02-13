@file:Suppress("PropertyName")

package dev.chara.taskify.shared.database

import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableCategory
import dev.chara.taskify.shared.model.MutableCategoryPrefs
import dev.chara.taskify.shared.model.MutableProfile
import dev.chara.taskify.shared.model.MutableTask
import dev.chara.taskify.shared.model.MutableWorkspace
import dev.chara.taskify.shared.model.SeedColor
import dev.chara.taskify.shared.model.Task
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.mongodb.kbson.ObjectId

@Serializable
internal class RealmId(internal val id: ObjectId) : Id() {
    override val hexString: String
        get() = id.toHexString()
}

fun Id.toObjectId() = if (this is RealmId) this.id else ObjectId(this.hexString)

fun ObjectId.toId(): Id = RealmId(this)

@Serializable
@PersistedName(name = "Workspace")
class RealmWorkspace : RealmObject, MutableWorkspace {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    @Ignore
    override val id: Id
        get() = _id.toId()

    override var ownerId: String = ""

    @Transient
    @PersistedName("members")
    var _members: RealmList<String> = realmListOf()

    @Ignore
    override val members: List<String>
        get() = _members

    override var name: String = ""

    @PersistedName("color")
    private var _color: String? = null

    @Ignore
    override var color: SeedColor?
        get() = _color?.let { enumValueOfOrNull<SeedColor>(it) }
        set(value) {
            _color = value?.name
        }
}

@Serializable
@PersistedName(name = "Category")
class RealmCategory : RealmObject, MutableCategory {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    @Ignore
    override val id: Id
        get() = _id.toId()

    @PersistedName("workspaceId")
    var _workspaceId: ObjectId? = null

    @Ignore
    override val workspaceId: Id?
        get() = _workspaceId?.toId()

    override var name: String = ""

    @PersistedName("classifier")
    private var _classifier: String? = null

    @Ignore
    override var classifier: Category.Classifier?
        get() = _classifier?.let { enumValueOfOrNull<Category.Classifier>(it) }
        set(value) {
            _classifier = value?.name
        }

    @PersistedName("color")
    private var _color: String? = null

    @Ignore
    override var color: SeedColor?
        get() = _color?.let { enumValueOfOrNull<SeedColor>(it) }
        set(value) {
            _color = value?.name
        }
}

@Serializable
@PersistedName(name = "CategoryPrefs")
class RealmCategoryPrefs : RealmObject, MutableCategoryPrefs {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    @PersistedName("workspaceId")
    var _workspaceId: ObjectId? = null

    @Ignore
    override val workspaceId: Id?
        get() = _workspaceId?.toId()

    @PersistedName("categoryId")
    var _categoryId: ObjectId? = null

    @Ignore
    override val categoryId: Id?
        get() = _categoryId?.toId()

    override var userId: String = ""

    override var ordinal: Int = 0

    @PersistedName("sortType")
    private var _sortType: String = CategoryPrefs.SortType.Name.name

    @Ignore
    override var sortType: CategoryPrefs.SortType
        get() = enumValueOfOrNull<CategoryPrefs.SortType>(_sortType) ?: CategoryPrefs.SortType.Name
        set(value) {
            _sortType = value.name
        }

    @PersistedName("sortDirection")
    private var _sortDirection: String = CategoryPrefs.SortDirection.Ascending.name

    @Ignore
    override var sortDirection: CategoryPrefs.SortDirection
        get() = enumValueOfOrNull<CategoryPrefs.SortDirection>(_sortDirection)
            ?: CategoryPrefs.SortDirection.Ascending
        set(value) {
            _sortDirection = value.name
        }

    override var topTasksCount: Int = 0
}

@Serializable
@PersistedName(name = "Task")
class RealmTask : RealmObject, MutableTask {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    @Ignore
    override val id: Id
        get() = _id.toId()

    @PersistedName("workspaceId")
    var _workspaceId: ObjectId? = null

    @Ignore
    override val workspaceId: Id?
        get() = _workspaceId?.toId()

    @PersistedName("categoryId")
    var _categoryId: ObjectId? = null

    @Ignore
    override val categoryId: Id?
        get() = _categoryId?.toId()

    override var name: String = ""
    override var group: String? = null

    @PersistedName("status")
    private var _status: String = Task.Status.NotStarted.name

    @Ignore
    override var status: Task.Status
        get() = enumValueOfOrNull<Task.Status>(_status) ?: Task.Status.NotStarted
        set(value) {
            _status = value.name
        }

    @PersistedName("scheduledAt")
    private var _scheduledAt: RealmInstant? = null

    @Ignore
    override var scheduledAt: LocalDateTime?
        get() = _scheduledAt?.getDateTime(TimeZone.currentSystemDefault())
        set(value) {
            _scheduledAt = value?.asRealmInstant(TimeZone.currentSystemDefault())
        }

    @PersistedName("assignedTo")
    var _assignedTo: RealmList<String> = realmListOf()

    @Ignore
    override var assignedTo: List<String>
        get() = _assignedTo
        set(value) {
            _assignedTo.clear()
            _assignedTo.addAll(value)
        }

    override var reminderFired: Boolean = false

    @PersistedName("lastModifiedAt")
    private var _lastModifiedAt: RealmInstant? = null

    @Ignore
    override var lastModifiedAt: Instant?
        get() = _lastModifiedAt?.getInstant()
        set(value) {
            _lastModifiedAt = value?.asRealmInstant()
        }

    override var lastModifiedBy: String? = null
}

@Serializable
@PersistedName(name = "UserInfo")
class RealmUserInfo : RealmObject, MutableProfile {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    override var userId: String = ""
    override var email: String = ""
}
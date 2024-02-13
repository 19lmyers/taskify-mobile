package dev.chara.taskify.shared.database

import dev.chara.taskify.shared.model.ListChange
import dev.chara.taskify.shared.model.ListChangeSet
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ListChangeSet.Range
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.SingleQueryChange
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.types.BaseRealmObject

class RealmInitial<out T>(override val list: List<T>) : ListChange.Initial<T>

class RealmUpdated<out T>(
    override val list: List<T>,
    override val deletions: IntArray,
    override val insertions: IntArray,
    override val changes: IntArray,
    override val deletionRanges: List<ListChangeSet.Range>,
    override val insertionRanges: List<ListChangeSet.Range>,
    override val changeRanges: List<ListChangeSet.Range>
) : ListChange.Updated<T>

fun Range.toModel() = ListChangeSet.Range(startIndex, length)
fun Array<Range>.toModel() = map { it.toModel() }

fun <T : BaseRealmObject> ResultsChange<out T>.toModel(): ListChange<T> =
    when (this) {
        is InitialResults -> RealmInitial(list)

        is UpdatedResults -> RealmUpdated(
            list,
            deletions,
            insertions,
            changes,
            deletionRanges.toModel(),
            insertionRanges.toModel(),
            changeRanges.toModel()
        )

    }

fun <T : BaseRealmObject> SingleQueryChange<out T>.toModel(): T? = this.obj
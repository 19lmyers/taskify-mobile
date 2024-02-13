package dev.chara.taskify.shared.model


sealed interface ListChange<out T> {
    val list: List<T>

    interface Initial<out T> : ListChange<T>
    interface Updated<out T> : ListChange<T>, ListChangeSet
}

interface ListChangeSet {
    val deletions: IntArray
    val insertions: IntArray
    val changes: IntArray

    val deletionRanges: List<Range>
    val insertionRanges: List<Range>
    val changeRanges: List<Range>

    data class Range(
        val startIndex: Int,
        val length: Int
    )
}
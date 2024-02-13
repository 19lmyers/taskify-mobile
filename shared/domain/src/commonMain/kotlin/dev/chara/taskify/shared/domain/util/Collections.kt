package dev.chara.taskify.shared.domain.util

import kotlin.jvm.JvmName

@JvmName("IterableAssociateWithNullable")
fun <K, V> Iterable<K?>.associateWithNullable(
    valueSelector: (K?) -> V,
): Map<K?, V> = buildMap {
    put(null, valueSelector(null))
    for (key in this@associateWithNullable) {
        put(key, valueSelector(key))
    }
}
package dev.chara.taskify.shared.database

import io.realm.kotlin.types.RealmInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun RealmInstant.getInstant(): Instant =
    Instant.fromEpochSeconds(epochSeconds, nanosecondsOfSecond)

fun Instant.asRealmInstant(): RealmInstant =
    RealmInstant.from(epochSeconds, nanosecondsOfSecond)

fun RealmInstant.getDateTime(zone: TimeZone) =
    getInstant().toLocalDateTime(zone)

fun LocalDateTime.asRealmInstant(zone: TimeZone) =
    toInstant(zone).asRealmInstant()
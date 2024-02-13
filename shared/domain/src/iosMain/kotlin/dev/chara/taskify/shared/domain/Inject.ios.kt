package dev.chara.taskify.shared.domain

import dev.chara.taskify.shared.datastore.DataStorePath
import org.koin.core.module.Module

actual fun Module.platform() {
    single { DataStorePath() }
}
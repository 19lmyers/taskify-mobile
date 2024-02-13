package dev.chara.taskify.shared.domain

import dev.chara.taskify.shared.datastore.DataStorePath
import dev.chara.taskify.shared.ml.AndroidModelLoader
import dev.chara.taskify.shared.ml.ModelLoader
import org.koin.core.module.Module

actual fun Module.platform() {
    single { DataStorePath(get()) }
    single<ModelLoader> { AndroidModelLoader() }
    single<ShareSheetManager> { AndroidShareSheetManager(get()) }
}

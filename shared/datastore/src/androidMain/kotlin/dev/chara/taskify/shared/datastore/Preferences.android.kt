package dev.chara.taskify.shared.datastore

import android.content.Context

actual class DataStorePath(private val context: Context) {
    actual fun get(fileName: String): String {
        return context.filesDir.resolve(fileName).absolutePath
    }
}

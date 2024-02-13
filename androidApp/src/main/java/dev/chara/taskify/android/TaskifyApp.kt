package dev.chara.taskify.android

import android.app.Application
import dev.chara.taskify.android.notification.AndroidMessagingToken
import dev.chara.taskify.shared.domain.MessagingToken
import dev.chara.taskify.shared.domain.domain
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

private fun android() = module {
    single<MessagingToken> { AndroidMessagingToken() }
}

class TaskifyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TaskifyApp)

            modules(android(), domain())
        }
    }
}

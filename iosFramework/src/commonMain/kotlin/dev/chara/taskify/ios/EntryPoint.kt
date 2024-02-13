@file:Suppress("unused")

package dev.chara.taskify.ios

import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.window.ComposeUIViewController
import dev.chara.taskify.shared.component.RootComponent
import dev.chara.taskify.shared.domain.MessagingToken
import dev.chara.taskify.shared.domain.ShareSheetManager
import dev.chara.taskify.shared.domain.domain
import dev.chara.taskify.shared.ml.ModelLoader
import dev.chara.taskify.shared.ui.content.RootContent
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun setupKoin(loader: ModelLoader, shareSheet: ShareSheetManager, token: MessagingToken) {
    startKoin { modules(apple(loader, shareSheet, token), domain()) }
}

private fun apple(
    loader: ModelLoader,
    shareSheet: ShareSheetManager,
    token: MessagingToken
) = module {
    single { loader }
    single { shareSheet }
    single { token }
}


@OptIn(ExperimentalComposeApi::class)
fun mainViewController(rootComponent: RootComponent) = ComposeUIViewController(configure = {
    platformLayers = false
}) {
    RootContent(component = rootComponent)
}

package dev.chara.taskify.android.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.defaultComponentContext
import dev.chara.taskify.shared.component.DeepLink
import dev.chara.taskify.shared.component.DefaultRootComponent
import dev.chara.taskify.shared.component.RootComponent
import dev.chara.taskify.shared.ui.content.RootContent


class MainActivity : ComponentActivity() {

    private lateinit var root: RootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            deepLink = parseIntent(intent)
        )

        setContent {
            RootContent(component = root)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        root.onDeepLink(parseIntent(intent))
    }

    private fun parseIntent(intent: Intent): DeepLink =
        if (intent.data != null && intent.data!!.host == DEEP_LINK_HOST) {
            when (intent.data!!.path) {
                PATH_JOIN_WORKSPACE -> {
                    val token = intent.data!!.getQueryParameter(QUERY_LIST_INVITE_TOKEN)

                    if (token != null) {
                        DeepLink.JoinWorkspace(token)
                    } else {
                        Toast.makeText(this, "Missing token parameter", Toast.LENGTH_LONG).show()
                        DeepLink.None
                    }
                }
                else -> {
                    DeepLink.None
                }
            }
        } else if (intent.action == ACTION_NEW_TASK) {
            DeepLink.CreateTask
        } else {
            DeepLink.None
        }

    companion object {
        const val DEEP_LINK_HOST = "taskify.chara.dev"

        const val PATH_JOIN_WORKSPACE = "/join"
        const val QUERY_LIST_INVITE_TOKEN = "token"

        const val ACTION_NEW_TASK = "dev.chara.taskify.android.ACTION_NEW_TASK"
    }
}

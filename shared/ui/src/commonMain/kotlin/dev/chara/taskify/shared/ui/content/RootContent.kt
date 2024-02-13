package dev.chara.taskify.shared.ui.content

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.chara.taskify.shared.component.RootComponent
import dev.chara.taskify.shared.ui.content.details.CategoryDetailsContent
import dev.chara.taskify.shared.ui.content.details.TaskDetailsContent
import dev.chara.taskify.shared.ui.content.home.HomeContent
import dev.chara.taskify.shared.ui.content.settings.SettingsContent
import dev.chara.taskify.shared.ui.content.welcome.SignInContent
import dev.chara.taskify.shared.ui.content.welcome.SignUpContent
import dev.chara.taskify.shared.ui.content.welcome.WelcomeContent
import dev.chara.taskify.shared.ui.theme.BaseTheme

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(component: RootComponent) {
    BaseTheme {
        Children(
            stack = component.childStack,
            animation = predictiveBackAnimation(
                backHandler = component.backHandler,
                fallbackAnimation = stackAnimation(fade()),
                onBack = component::onBack,
            )
        ) {
            when (val child = it.instance) {
                is RootComponent.Child.Welcome -> WelcomeContent(child.component)
                is RootComponent.Child.SignUp -> SignUpContent(child.component)
                is RootComponent.Child.SignIn -> SignInContent(child.component)
                is RootComponent.Child.Home -> HomeContent(child.component)
                is RootComponent.Child.CategoryDetails -> CategoryDetailsContent(child.component)
                is RootComponent.Child.TaskDetails -> TaskDetailsContent(child.component)
                is RootComponent.Child.Settings -> SettingsContent(child.component)
            }
        }
    }
}
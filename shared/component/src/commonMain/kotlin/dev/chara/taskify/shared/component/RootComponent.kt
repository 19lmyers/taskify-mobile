package dev.chara.taskify.shared.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.component.RootComponent.Child
import dev.chara.taskify.shared.component.details.CategoryDetailsComponent
import dev.chara.taskify.shared.component.details.DefaultCategoryDetailsComponent
import dev.chara.taskify.shared.component.details.DefaultTaskDetailsComponent
import dev.chara.taskify.shared.component.details.TaskDetailsComponent
import dev.chara.taskify.shared.component.home.DefaultHomeComponent
import dev.chara.taskify.shared.component.home.HomeComponent
import dev.chara.taskify.shared.component.settings.DefaultSettingsComponent
import dev.chara.taskify.shared.component.settings.SettingsComponent
import dev.chara.taskify.shared.component.welcome.DefaultSignInComponent
import dev.chara.taskify.shared.component.welcome.DefaultSignUpComponent
import dev.chara.taskify.shared.component.welcome.DefaultWelcomeComponent
import dev.chara.taskify.shared.component.welcome.SignInComponent
import dev.chara.taskify.shared.component.welcome.SignUpComponent
import dev.chara.taskify.shared.component.welcome.WelcomeComponent
import dev.chara.taskify.shared.domain.use_case.account.GetCurrentProfileUseCase
import dev.chara.taskify.shared.domain.use_case.account.LinkFcmTokenUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.UpdateTaskUseCase
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Task
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RootComponent : BackHandlerOwner {

    val childStack: Value<ChildStack<*, Child>>

    fun onDeepLink(deepLink: DeepLink)

    fun linkFCMToken(token: String)

    fun markTaskAsCompleted(taskId: String)

    fun onBack()

    sealed interface Child {
        class Welcome(val component: WelcomeComponent) : Child

        class SignUp(val component: SignUpComponent) : Child

        class SignIn(val component: SignInComponent) : Child

        class Home(val component: HomeComponent) : Child

        class CategoryDetails(val component: CategoryDetailsComponent) : Child

        class TaskDetails(val component: TaskDetailsComponent) : Child

        class Settings(val component: SettingsComponent) : Child
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext, deepLink: DeepLink = DeepLink.None
) : RootComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val navigation = StackNavigation<Config>()

    private val getCurrentProfileUseCase: GetCurrentProfileUseCase by inject()
    private val linkFcmTokenUseCase: LinkFcmTokenUseCase by inject()

    private val updateTaskUseCase: UpdateTaskUseCase by inject()

    private val profile = getCurrentProfileUseCase()

    private val stack = childStack(
        source = navigation, serializer = Config.serializer(), initialStack = {
            if (profile == null) {
                listOf(Config.Welcome)
            } else {
                getStackFor(deepLink)
            }
        }, childFactory = ::child, handleBackButton = true
    )

    override val childStack: Value<ChildStack<*, Child>> = stack

    private fun child(config: Config, componentContext: ComponentContext): Child = when (config) {
        is Config.Home -> Child.Home(
            DefaultHomeComponent(componentContext,
                preselectWorkspace = config.preselectWorkspace,
                showCreateTaskSheet = config.showCreateTask,
                showJoinWorkspaceFor = config.showJoinWorkspaceFor,
                navigateToWelcome = {
                    navigation.replaceAll(Config.Welcome)
                },
                navigateToCategoryDetails = { workspaceId, categoryId ->
                    navigation.push(Config.CategoryDetails(workspaceId, categoryId))
                },
                navigateToTaskDetails = { workspaceId, taskId ->
                    navigation.push(Config.TaskDetails(workspaceId, taskId))
                })
        )

        is Config.CategoryDetails -> Child.CategoryDetails(
            DefaultCategoryDetailsComponent(componentContext,
                workspaceId = config.workspaceId,
                categoryId = config.categoryId,
                navigateUp = {
                    navigation.pop()
                },
                navigateToTaskDetails = { workspaceId, taskId ->
                    navigation.push(Config.TaskDetails(workspaceId, taskId))
                })
        )

        is Config.TaskDetails -> Child.TaskDetails(
            DefaultTaskDetailsComponent(componentContext,
                workspaceId = config.workspaceId,
                taskId = config.taskId,
                navigateUp = {
                    navigation.pop()
                })
        )

        Config.Settings -> Child.Settings(DefaultSettingsComponent(componentContext, navigateUp = {
            navigation.pop()
        }))

        Config.SignIn -> Child.SignIn(DefaultSignInComponent(componentContext, navigateUp = {
            navigation.pop()
        }, navigateToHome = {
            navigation.replaceAll(Config.Home())
        }))

        Config.SignUp -> Child.SignUp(DefaultSignUpComponent(componentContext, navigateUp = {
            navigation.pop()
        }, navigateToHome = {
            navigation.replaceAll(Config.Home())
        }))

        Config.Welcome -> Child.Welcome(DefaultWelcomeComponent(componentContext,
            navigateToSignIn = {
                navigation.push(Config.SignIn)
            },
            navigateToSignUp = {
                navigation.push(Config.SignUp)
            })
        )
    }

    override fun onDeepLink(deepLink: DeepLink) {
        navigation.replaceAll(*getStackFor(deepLink).toTypedArray())
    }

    override fun linkFCMToken(token: String) {
        coroutineScope.launch {
            linkFcmTokenUseCase(token)
        }
    }

    private class ReceivedId(override val hexString: String) : Id()

    override fun markTaskAsCompleted(taskId: String) {
        coroutineScope.launch {
            updateTaskUseCase(ReceivedId(taskId)) {
                it.status = Task.Status.Complete
            }
        }
    }

    override fun onBack() {
        navigation.pop()
    }

    private companion object {
        private fun getStackFor(deepLink: DeepLink): List<Config> = when (deepLink) {
            DeepLink.None -> listOf(Config.Home())
            DeepLink.CreateTask -> listOf(Config.Home(showCreateTask = true))
            is DeepLink.ViewWorkspace -> listOf(Config.Home(preselectWorkspace = ReceivedId(deepLink.id)))

            is DeepLink.ViewTask -> listOf(
                Config.Home(preselectWorkspace = ReceivedId(deepLink.workspaceId)),
                Config.TaskDetails(
                    workspaceId = ReceivedId(deepLink.workspaceId),
                    taskId = ReceivedId(deepLink.taskId)
                )
            )

            is DeepLink.JoinWorkspace -> listOf(Config.Home(showJoinWorkspaceFor = deepLink.token))
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Welcome : Config

        @Serializable
        data object SignUp : Config

        @Serializable
        data object SignIn : Config

        @Serializable
        data class Home(
            @Serializable(with = IdSerializer::class) val preselectWorkspace: Id? = null,
            val showCreateTask: Boolean = false,
            val showJoinWorkspaceFor: String? = null
        ) : Config

        @Serializable
        data class CategoryDetails(
            @Serializable(with = IdSerializer::class) val workspaceId: Id,
            @Serializable(with = IdSerializer::class) val categoryId: Id?
        ) : Config

        @Serializable
        data class TaskDetails(
            @Serializable(with = IdSerializer::class) val workspaceId: Id,
            @Serializable(with = IdSerializer::class) val taskId: Id
        ) : Config

        @Serializable
        data object Settings : Config
    }
}
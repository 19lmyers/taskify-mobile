package dev.chara.taskify.shared.component.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.component.IdSerializer
import dev.chara.taskify.shared.component.home.HomeComponent.Child
import dev.chara.taskify.shared.component.sheet.CategorySheetComponent
import dev.chara.taskify.shared.component.sheet.CreateCategoryComponent
import dev.chara.taskify.shared.component.sheet.CreateTaskComponent
import dev.chara.taskify.shared.component.sheet.CreateWorkspaceComponent
import dev.chara.taskify.shared.component.sheet.DefaultPrefsSheetComponent
import dev.chara.taskify.shared.component.sheet.JoinSheetComponent
import dev.chara.taskify.shared.component.sheet.JoinWorkspaceComponent
import dev.chara.taskify.shared.component.sheet.PrefsSheetComponent
import dev.chara.taskify.shared.component.sheet.ShareSheetComponent
import dev.chara.taskify.shared.component.sheet.ShareWorkspaceComponent
import dev.chara.taskify.shared.component.sheet.TaskSheetComponent
import dev.chara.taskify.shared.component.sheet.UpdateCategoryComponent
import dev.chara.taskify.shared.component.sheet.UpdateWorkspaceComponent
import dev.chara.taskify.shared.component.sheet.WorkspaceSheetComponent
import dev.chara.taskify.shared.domain.use_case.GetSelectedWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.SelectWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.account.GetCurrentProfileUseCase
import dev.chara.taskify.shared.domain.use_case.account.SignOutUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.DeleteTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.UpdateTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.CreateWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.DeleteWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.home.GetHomeContentUseCase
import dev.chara.taskify.shared.domain.use_case.home.GetHomeContentUseCase.CategoryEntry
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableTask
import dev.chara.taskify.shared.model.MutableWorkspace
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val workspaces: List<Workspace> = emptyList(),
    val workspace: Workspace? = null,
    val members: List<Profile> = emptyList(),
    val currentUser: Profile? = null,
    val categories: List<Category> = emptyList(),
    val suggestedTasks: List<Task> = emptyList(),
    val otherTasks: Map<Category?, CategoryEntry> = emptyMap()
)

interface HomeComponent {
    val state: StateFlow<HomeUiState>
    val sheet: Value<ChildSlot<*, Child>>

    fun showCreateWorkspace()

    fun showUpdateWorkspace(workspaceId: Id)

    fun showCustomizeWorkspace(workspaceId: Id)

    fun showShareWorkspace(workspaceId: Id)

    fun showCreateCategory(workspaceId: Id)

    fun showUpdateCategory(categoryId: Id)

    fun showCreateTask(workspaceId: Id)

    fun showCategoryDetails(workspaceId: Id, categoryId: Id?)

    fun showTaskDetails(workspaceId: Id, taskId: Id)

    fun createWorkspace(builder: (MutableWorkspace) -> Unit)

    fun deleteWorkspace(workspaceId: Id)

    fun selectWorkspace(workspaceId: Id)

    fun updateTask(taskId: Id, builder: (MutableTask) -> Unit)

    fun deleteTask(taskId: Id)

    fun signOut()

    sealed interface Child {
        class WorkspaceSheet(val component: WorkspaceSheetComponent) : Child
        class PrefsSheet(val component: PrefsSheetComponent) : Child
        class ShareSheet(val component: ShareSheetComponent) : Child
        class JoinSheet(val component: JoinSheetComponent) : Child
        class CategorySheet(val component: CategorySheetComponent) : Child
        class TaskSheet(val component: TaskSheetComponent) : Child
    }
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    preselectWorkspace: Id?,
    showCreateTaskSheet: Boolean,
    showJoinWorkspaceFor: String?,
    private val navigateToWelcome: () -> Unit,
    private val navigateToCategoryDetails: (Id, Id?) -> Unit,
    private val navigateToTaskDetails: (Id, Id) -> Unit
) : HomeComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getCurrentProfileUseCase: GetCurrentProfileUseCase by inject()

    private val getHomeContentUseCase: GetHomeContentUseCase by inject()

    private val getSelectedWorkspaceUseCase: GetSelectedWorkspaceUseCase by inject()
    private val selectWorkspaceUseCase: SelectWorkspaceUseCase by inject()

    private val createWorkspaceUseCase: CreateWorkspaceUseCase by inject()
    private val deleteWorkspaceUseCase: DeleteWorkspaceUseCase by inject()

    private val updateTaskUseCase: UpdateTaskUseCase by inject()
    private val deleteTaskUseCase: DeleteTaskUseCase by inject()

    private val signOutUseCase: SignOutUseCase by inject()

    private var _state = MutableStateFlow(HomeUiState(isLoading = true))
    override val state = _state.asStateFlow()

    private val navigation = SlotNavigation<Config>()

    override val sheet: Value<ChildSlot<*, Child>> = childSlot(
        source = navigation,
        serializer = Config.serializer(),
        childFactory = ::child,
        handleBackButton = true
    )

    private fun child(
        config: Config, componentContext: ComponentContext
    ): Child = when (config) {
        Config.CreateWorkspace -> Child.WorkspaceSheet(
            CreateWorkspaceComponent(componentContext, dismiss = {
                navigation.dismiss()
            })
        )

        is Config.UpdateWorkspace -> Child.WorkspaceSheet(
            UpdateWorkspaceComponent(componentContext, config.workspaceId, dismiss = {
                navigation.dismiss()
            })
        )

        is Config.CustomizeWorkspace -> Child.PrefsSheet(
            DefaultPrefsSheetComponent(componentContext, config.workspaceId, dismiss = {
                navigation.dismiss()
            })
        )

        is Config.ShareWorkspace -> Child.ShareSheet(
            ShareWorkspaceComponent(componentContext, config.workspaceId, dismiss = {
                navigation.dismiss()
            })
        )

        is Config.JoinWorkspace -> Child.JoinSheet(
            JoinWorkspaceComponent(componentContext, config.inviteToken, dismiss = {
                navigation.dismiss()
            })
        )

        is Config.CreateCategory -> Child.CategorySheet(
            CreateCategoryComponent(componentContext, config.workspaceId, dismiss = {
                navigation.dismiss()
            })
        )

        is Config.UpdateCategory -> Child.CategorySheet(
            UpdateCategoryComponent(componentContext, config.categoryId, dismiss = {
                navigation.dismiss()
            })
        )

        is Config.CreateTask -> Child.TaskSheet(
            CreateTaskComponent(componentContext,
                config.workspaceId,
                categoryId = null, //TODO last category ID?
                dismiss = {
                    navigation.dismiss()
                })
        )
    }

    init {
        coroutineScope.launch {
            // Select workspace if opened
            preselectWorkspace?.let {
                selectWorkspaceUseCase(it)
            }

            // Open create task sheet on component creation, if requested
            val selected = getSelectedWorkspaceUseCase().first()
            if (showCreateTaskSheet && selected != null) {
                showCreateTask(selected)
            }

            showJoinWorkspaceFor?.let {
                navigation.activate(Config.JoinWorkspace(it))
            }

            getHomeContentUseCase().collect { result ->
                _state.value = HomeUiState(
                    isLoading = false,
                    workspaces = result.workspaces,
                    currentUser = getCurrentProfileUseCase(),
                    members = result.members,
                    workspace = result.selectedWorkspace,
                    categories = result.categories,
                    suggestedTasks = result.suggestedTasks,
                    otherTasks = result.otherTasks
                )
            }
        }
    }

    override fun showCreateWorkspace() = navigation.activate(Config.CreateWorkspace)

    override fun showUpdateWorkspace(workspaceId: Id) =
        navigation.activate(Config.UpdateWorkspace(workspaceId))

    override fun showCustomizeWorkspace(workspaceId: Id) =
        navigation.activate(Config.CustomizeWorkspace(workspaceId))

    override fun showShareWorkspace(workspaceId: Id) =
        navigation.activate(Config.ShareWorkspace(workspaceId))

    override fun showCreateCategory(workspaceId: Id) =
        navigation.activate(Config.CreateCategory(workspaceId))

    override fun showUpdateCategory(categoryId: Id) =
        navigation.activate(Config.UpdateCategory(categoryId))

    override fun showCreateTask(workspaceId: Id) =
        navigation.activate(Config.CreateTask(workspaceId))

    override fun showCategoryDetails(workspaceId: Id, categoryId: Id?) =
        navigateToCategoryDetails(workspaceId, categoryId)

    override fun showTaskDetails(workspaceId: Id, taskId: Id) =
        navigateToTaskDetails(workspaceId, taskId)

    override fun createWorkspace(builder: (MutableWorkspace) -> Unit) {
        coroutineScope.launch {
            createWorkspaceUseCase(builder)
        }
    }

    override fun deleteWorkspace(workspaceId: Id) {
        coroutineScope.launch {
            deleteWorkspaceUseCase(workspaceId)
        }
    }

    override fun selectWorkspace(workspaceId: Id) {
        _state.value = _state.value.copy(
            isLoading = true
        )
        coroutineScope.launch {
            selectWorkspaceUseCase(workspaceId)
        }
    }

    override fun updateTask(taskId: Id, builder: (MutableTask) -> Unit) {
        coroutineScope.launch {
            updateTaskUseCase(taskId, builder)
        }
    }

    override fun deleteTask(taskId: Id) {
        coroutineScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    override fun signOut() {
        coroutineScope.launch { signOutUseCase() }
        navigateToWelcome()
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object CreateWorkspace : Config

        @Serializable
        data class UpdateWorkspace(
            @Serializable(with = IdSerializer::class) val workspaceId: Id
        ) : Config

        @Serializable
        data class CustomizeWorkspace(
            @Serializable(with = IdSerializer::class) val workspaceId: Id
        ) : Config

        @Serializable
        data class ShareWorkspace(
            @Serializable(with = IdSerializer::class) val workspaceId: Id
        ) : Config

        @Serializable
        data class JoinWorkspace(
            val inviteToken: String
        ) : Config

        @Serializable
        data class CreateCategory(
            @Serializable(with = IdSerializer::class) val workspaceId: Id
        ) : Config

        @Serializable
        data class UpdateCategory(
            @Serializable(with = IdSerializer::class) val categoryId: Id
        ) : Config

        @Serializable
        data class CreateTask(
            @Serializable(with = IdSerializer::class) val workspaceId: Id
        ) : Config
    }
}
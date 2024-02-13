package dev.chara.taskify.shared.component.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.component.IdSerializer
import dev.chara.taskify.shared.component.details.CategoryDetailsComponent.Child
import dev.chara.taskify.shared.component.sheet.CategorySheetComponent
import dev.chara.taskify.shared.component.sheet.CreateTaskComponent
import dev.chara.taskify.shared.component.sheet.TaskSheetComponent
import dev.chara.taskify.shared.component.sheet.UpdateCategoryComponent
import dev.chara.taskify.shared.domain.use_case.account.GetCurrentProfileUseCase
import dev.chara.taskify.shared.domain.use_case.data.category_prefs.UpdateCategoryPrefsUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.DeleteAllCompletedTasksForCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.DeleteTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.UpdateTaskUseCase
import dev.chara.taskify.shared.domain.use_case.details.GetCategoryDetailsUseCase
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableCategoryPrefs
import dev.chara.taskify.shared.model.MutableTask
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class CategoryDetailsUiState(
    val isLoading: Boolean = false,
    val workspace: Workspace? = null,
    val members: List<Profile> = emptyList(),
    val currentUser: Profile? = null,
    val category: Category? = null,
    val prefs: CategoryPrefs? = null,
    val currentTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList()
)

interface CategoryDetailsComponent {
    val state: StateFlow<CategoryDetailsUiState>
    val sheet: Value<ChildSlot<*, Child>>

    fun onUp()

    fun showUpdateCategory()

    fun showCreateTask()

    fun showTaskDetails(taskId: Id)

    fun updateCategoryPrefs(builder: (MutableCategoryPrefs) -> Unit)

    fun updateTask(taskId: Id, builder: (MutableTask) -> Unit)

    fun deleteTask(taskId: Id)

    fun deleteAllCompletedTasks()

    sealed interface Child {
        class CategorySheet(val component: CategorySheetComponent) : Child
        class TaskSheet(val component: TaskSheetComponent) : Child
    }
}

class DefaultCategoryDetailsComponent(
    componentContext: ComponentContext,
    private val workspaceId: Id,
    private val categoryId: Id?,
    private val navigateUp: () -> Unit,
    private val navigateToTaskDetails: (Id, Id) -> Unit
) : CategoryDetailsComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getCurrentProfileUseCase: GetCurrentProfileUseCase by inject()
    private val getCategoryDetailsUseCase: GetCategoryDetailsUseCase by inject()
    private val updateCategoryPrefsUseCase: UpdateCategoryPrefsUseCase by inject()
    private val updateTaskUseCase: UpdateTaskUseCase by inject()
    private val deleteTaskUseCase: DeleteTaskUseCase by inject()
    private val deleteCompletedTasksUseCase: DeleteAllCompletedTasksForCategoryUseCase by inject()

    private var _state = MutableStateFlow(CategoryDetailsUiState(isLoading = true))
    override val state = _state.asStateFlow()

    private val navigation = SlotNavigation<Config>()

    override val sheet: Value<ChildSlot<*, Child>> =
        childSlot(
            source = navigation,
            serializer = Config.serializer(),
            childFactory = ::child,
            handleBackButton = true
        )

    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): Child = when (config) {

        is Config.UpdateCategory -> Child.CategorySheet(
            UpdateCategoryComponent(
                componentContext,
                config.categoryId,
                dismiss = {  deleted ->
                    navigation.dismiss()
                    if (deleted) {
                        navigateUp()
                    }
                }
            )
        )

        is Config.CreateTask -> Child.TaskSheet(
            CreateTaskComponent(
                componentContext,
                config.workspaceId,
                config.categoryId,
                dismiss = {
                    navigation.dismiss()
                }
            )
        )
    }

    init {
        coroutineScope.launch {
            getCategoryDetailsUseCase(workspaceId, categoryId).collect {
                _state.value = CategoryDetailsUiState(
                    isLoading = false,
                    workspace = it.workspace,
                    members = it.members,
                    currentUser = getCurrentProfileUseCase(),
                    category = it.category,
                    prefs = it.prefs,
                    currentTasks = it.currentTasks,
                    completedTasks = it.completedTasks
                )
            }
        }
    }

    override fun onUp() = navigateUp()

    override fun showUpdateCategory() = categoryId?.let {
        navigation.activate(Config.UpdateCategory(it))
    } ?: Unit

    override fun showCreateTask() = navigation.activate(Config.CreateTask(workspaceId, categoryId))

    override fun showTaskDetails(taskId: Id) = navigateToTaskDetails(workspaceId, taskId)

    override fun updateCategoryPrefs(builder: (MutableCategoryPrefs) -> Unit) {
        coroutineScope.launch {
            updateCategoryPrefsUseCase(workspaceId, categoryId, builder)
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

    override fun deleteAllCompletedTasks() {
        coroutineScope.launch {
            deleteCompletedTasksUseCase(workspaceId, categoryId)
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data class UpdateCategory(
            @Serializable(with = IdSerializer::class)
            val categoryId: Id
        ) : Config

        @Serializable
        data class CreateTask(
            @Serializable(with = IdSerializer::class)
            val workspaceId: Id,

            @Serializable(with = IdSerializer::class)
            val categoryId: Id?
        ) : Config
    }
}
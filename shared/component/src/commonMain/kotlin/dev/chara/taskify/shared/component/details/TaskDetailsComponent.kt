package dev.chara.taskify.shared.component.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.domain.use_case.account.GetCurrentProfileUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.DeleteTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.UpdateAndMoveTaskUseCase
import dev.chara.taskify.shared.domain.use_case.details.GetTaskDetailsUseCase
import dev.chara.taskify.shared.domain.use_case.ml.ClassifyTaskUseCase
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableTask
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class TaskDetailsUiState(
    val isLoading: Boolean = false,
    val workspace: Workspace? = null,
    val currentUser: Profile? = null,
    val members: List<Profile> = emptyList(),
    val categories: List<Category> = emptyList(),
    val task: Task? = null,
    val classification: String? = null,
    val lastModifiedBy: Profile? = null
)

interface TaskDetailsComponent : BackHandlerOwner {
    val state: StateFlow<TaskDetailsUiState>

    fun onUp()

    fun reclassify(categoryId: Id?, name: String, classifierType: Category.Classifier?)

    fun save(categoryId: Id?, builder: (MutableTask) -> Unit)

    fun delete()
}

class DefaultTaskDetailsComponent(
    componentContext: ComponentContext,
    private val workspaceId: Id,
    private val taskId: Id,
    private val navigateUp: () -> Unit
) : TaskDetailsComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getCurrentProfileUseCase: GetCurrentProfileUseCase by inject()
    private val getTaskDetailsUseCase: GetTaskDetailsUseCase by inject()
    private val classifyTaskUseCase: ClassifyTaskUseCase by inject()
    private val updateAndMoveTaskUseCase: UpdateAndMoveTaskUseCase by inject()
    private val deleteTaskUseCase: DeleteTaskUseCase by inject()

    private var _state = MutableStateFlow(TaskDetailsUiState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            getTaskDetailsUseCase(workspaceId, taskId).collect {
                _state.value = _state.value.copy(
                    isLoading = false,
                    workspace = it.workspace,
                    currentUser = getCurrentProfileUseCase(),
                    members = it.members,
                    categories = it.categories,
                    task = it.task,
                    classification = null,
                    lastModifiedBy = it.lastModifiedBy
                )
            }
        }
    }

    override fun onUp() = navigateUp()

    override fun reclassify(categoryId: Id?, name: String, classifierType: Category.Classifier?) {
        coroutineScope.launch {
            val group = classifyTaskUseCase(name, classifierType)
            _state.value = _state.value.copy(classification = group)
        }
    }

    override fun save(categoryId: Id?, builder: (MutableTask) -> Unit) {
        coroutineScope.launch {
            updateAndMoveTaskUseCase(taskId, categoryId, builder)
        }
    }

    override fun delete() {
        coroutineScope.launch {
            deleteTaskUseCase(taskId)
            navigateUp()
        }
    }
}
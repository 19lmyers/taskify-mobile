package dev.chara.taskify.shared.component.sheet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.domain.use_case.GetWorkspaceMembersUseCase
import dev.chara.taskify.shared.domain.use_case.account.GetCurrentProfileUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.GetCategoriesForWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.CreateTaskUseCase
import dev.chara.taskify.shared.domain.use_case.ml.ClassifyTaskUseCase
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableTask
import dev.chara.taskify.shared.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class TaskSheetUiState(
    val isLoading: Boolean = false,
    val currentUser: Profile? = null,
    val members: List<Profile> = emptyList(),
    val initialCategory: Id? = null,
    val categories: List<Category> = emptyList(),
    val creationInProgress: Boolean = false,
)

interface TaskSheetComponent {
    val state: StateFlow<TaskSheetUiState>

    fun cancel()

    fun save(categoryId: Id?, classifierType: Category.Classifier?, name: String, builder: (MutableTask) -> Unit)
}

class CreateTaskComponent(
    componentContext: ComponentContext,
    private val workspaceId: Id,
    private val categoryId: Id?,
    private val dismiss: () -> Unit
) : TaskSheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getCurrentProfile: GetCurrentProfileUseCase by inject()
    private val getWorkspaceMembersUseCase: GetWorkspaceMembersUseCase by inject()
    private val getCategoriesUseCase: GetCategoriesForWorkspaceUseCase by inject()

    private val classifyTaskUseCase: ClassifyTaskUseCase by inject()

    private val createTaskUseCase: CreateTaskUseCase by inject()

    private var _state = MutableStateFlow(TaskSheetUiState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            combine(getWorkspaceMembersUseCase(workspaceId), getCategoriesUseCase(workspaceId)) { members, categories ->
                TaskSheetUiState(
                    members = members,
                    categories = categories
                )
            }.collect {
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentUser = getCurrentProfile(),
                    members = it.members,
                    initialCategory = categoryId,
                    categories = it.categories
                )
            }
        }
    }

    override fun cancel() = dismiss()

    override fun save(categoryId: Id?, classifierType: Category.Classifier?, name: String, builder: (MutableTask) -> Unit) {
        coroutineScope.launch {
            _state.value = _state.value.copy(creationInProgress = true)

            val group = classifierType?.let {
                classifyTaskUseCase(name, classifierType)
            }

            createTaskUseCase(workspaceId, categoryId) {
                it.group = group
                it.apply(builder)
            }
            dismiss()
        }
    }
}
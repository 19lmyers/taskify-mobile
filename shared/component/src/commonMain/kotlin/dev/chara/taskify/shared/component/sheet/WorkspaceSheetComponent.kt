package dev.chara.taskify.shared.component.sheet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.domain.use_case.account.GetCurrentProfileUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.CreateWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.DeleteWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.GetWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.UpdateWorkspaceUseCase
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableWorkspace
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WorkspaceSheetUiState(
    val isLoading: Boolean = false,
    val creationInProgress: Boolean = false,
    val deletionInProgress: Boolean = false,
    val current: Profile? = null,
    val workspace: Workspace? = null
)

interface WorkspaceSheetComponent {
    val state: StateFlow<WorkspaceSheetUiState>

    fun cancel()

    fun save(builder: (MutableWorkspace) -> Unit)

    fun delete()
}

class CreateWorkspaceComponent(
    componentContext: ComponentContext,
    private val dismiss: () -> Unit
) : WorkspaceSheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val createWorkspaceUseCase: CreateWorkspaceUseCase by inject()

    private var _state = MutableStateFlow(WorkspaceSheetUiState())
    override val state = _state.asStateFlow()

    override fun cancel() = dismiss()

    override fun save(builder: (MutableWorkspace) -> Unit) {
        coroutineScope.launch {
            _state.value = _state.value.copy(creationInProgress = true)
            createWorkspaceUseCase(builder)
            dismiss()
        }
    }

    override fun delete() = Unit
}

class UpdateWorkspaceComponent(
    componentContext: ComponentContext,
    private val workspaceId: Id,
    private val dismiss: () -> Unit
) : WorkspaceSheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getCurrentProfileUseCase: GetCurrentProfileUseCase by inject()

    private val getWorkspaceUseCase: GetWorkspaceUseCase by inject()
    private val updateWorkspaceUseCase: UpdateWorkspaceUseCase by inject()
    private val deleteWorkspaceUseCase: DeleteWorkspaceUseCase by inject()

    private var _state = MutableStateFlow(WorkspaceSheetUiState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            getWorkspaceUseCase(workspaceId).collect {
                _state.value = _state.value.copy(
                    isLoading = false,
                    current = getCurrentProfileUseCase(),
                    workspace = it
                )
            }
        }
    }

    override fun cancel() = dismiss()

    override fun save(builder: (MutableWorkspace) -> Unit) {
        coroutineScope.launch {
            updateWorkspaceUseCase(workspaceId, builder)
            dismiss()
        }
    }

    override fun delete() {
        coroutineScope.launch {
            _state.value = _state.value.copy(deletionInProgress = true)
            deleteWorkspaceUseCase(workspaceId)
            dismiss()
        }
    }
}

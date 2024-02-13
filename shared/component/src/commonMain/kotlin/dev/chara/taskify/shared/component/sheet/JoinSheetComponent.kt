package dev.chara.taskify.shared.component.sheet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.domain.use_case.SelectWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.membership.GetInviteDetailsUseCase
import dev.chara.taskify.shared.domain.use_case.membership.JoinWorkspaceUseCase
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class JoinSheetUiState(
    val isLoading: Boolean = false,
    val inviteIsValid: Boolean = false,
    val workspace: Workspace? = null,
    val owner: Profile? = null,
    val members: List<Profile> = emptyList(),
    val joinInProgress: Boolean = false
)


interface JoinSheetComponent {
    val state: StateFlow<JoinSheetUiState>

    fun cancel()

    fun join()
}

class JoinWorkspaceComponent(
    componentContext: ComponentContext,
    private val inviteToken: String,
    private val dismiss: () -> Unit
) : JoinSheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getInviteDetailsUseCase: GetInviteDetailsUseCase by inject()
    private val joinWorkspaceUseCase: JoinWorkspaceUseCase by inject()
    private val selectWorkspaceUseCase: SelectWorkspaceUseCase by inject()

    private var _state = MutableStateFlow(JoinSheetUiState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            getInviteDetailsUseCase(inviteToken).collect {
                _state.value = _state.value.copy(
                    isLoading = false,
                    inviteIsValid = it.inviteIsValid,
                    workspace = it.workspace,
                    owner = it.owner,
                    members = it.members
                )
            }
        }
    }

    override fun cancel() = dismiss()

    override fun join() {
        coroutineScope.launch {
            _state.value = _state.value.copy(joinInProgress = true)
            val result = joinWorkspaceUseCase(inviteToken)
            if (result) {
                selectWorkspaceUseCase(state.value.workspace!!.id)
                dismiss()
            }
            _state.value = _state.value.copy(joinInProgress = false)
        }
    }
}
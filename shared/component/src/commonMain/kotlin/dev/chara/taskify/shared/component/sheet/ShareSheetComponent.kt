package dev.chara.taskify.shared.component.sheet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.domain.use_case.ShareWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.home.GetSharingInfoUseCase
import dev.chara.taskify.shared.domain.use_case.membership.CreateInviteTokenUseCase
import dev.chara.taskify.shared.domain.use_case.membership.LeaveWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.membership.RemoveMemberFromWorkspaceUseCase
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ShareSheetUiState(
    val isLoading: Boolean = false,
    val current: Profile? = null,
    val workspace: Workspace? = null,
    val owner: Profile? = null,
    val members: List<Profile> = emptyList(),
    val leaveInProgress: Boolean = false
)

interface ShareSheetComponent {
    val state: StateFlow<ShareSheetUiState>

    fun cancel()

    fun shareWorkspace()

    fun leaveWorkspace()

    fun removeMember(userId: String)
}

class ShareWorkspaceComponent(
    componentContext: ComponentContext,
    private val workspaceId: Id,
    private val dismiss: () -> Unit
) : ShareSheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getSharingInfoUseCase: GetSharingInfoUseCase by inject()

    private val createInviteTokenUseCase: CreateInviteTokenUseCase by inject()
    private val shareWorkspaceUseCase: ShareWorkspaceUseCase by inject()
    private val leaveWorkspaceUseCase: LeaveWorkspaceUseCase by inject()
    private val removeMemberFromWorkspaceUseCase: RemoveMemberFromWorkspaceUseCase by inject()

    private var _state = MutableStateFlow(ShareSheetUiState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            getSharingInfoUseCase(workspaceId).collect {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        current = it.current,
                        workspace = it.workspace,
                        owner = it.owner,
                        members = it.members
                    )
            }
        }
    }

    override fun cancel() = dismiss()

    override fun shareWorkspace() {
        coroutineScope.launch {
            createInviteTokenUseCase(workspaceId)?.let { shareWorkspaceUseCase(it) }
        }
    }

    override fun leaveWorkspace() {
        coroutineScope.launch {
            _state.value = _state.value.copy(leaveInProgress = true)
            val result = leaveWorkspaceUseCase(workspaceId)
            if (result) {
                dismiss()
            }
            _state.value = _state.value.copy(leaveInProgress = false)
        }
    }

    override fun removeMember(userId: String) {
        coroutineScope.launch {
            removeMemberFromWorkspaceUseCase(workspaceId, userId)
        }
    }
}

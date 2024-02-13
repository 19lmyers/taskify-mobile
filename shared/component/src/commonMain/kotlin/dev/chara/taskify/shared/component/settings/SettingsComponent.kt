package dev.chara.taskify.shared.component.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent

data class SettingsUiState(
    val isLoading: Boolean = false,
)

interface SettingsComponent {
    val state: StateFlow<SettingsUiState>

    fun onUp()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val navigateUp: () -> Unit
) : SettingsComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private var _state = MutableStateFlow(SettingsUiState(isLoading = true))
    override val state = _state.asStateFlow()

    override fun onUp() = navigateUp()
}
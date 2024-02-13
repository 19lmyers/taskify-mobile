package dev.chara.taskify.shared.component.welcome

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrapError
import dev.chara.taskify.shared.domain.use_case.account.SignInUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class SignInUiState(val isLoading: Boolean = false)

interface SignInComponent {
    val state: StateFlow<SignInUiState>

    fun onUp()

    fun signIn(email: String, password: String)
}

class DefaultSignInComponent(
    componentContext: ComponentContext,
    private val navigateUp: () -> Unit,
    private val navigateToHome: () -> Unit,
) : SignInComponent, KoinComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()

    private val signInUseCase: SignInUseCase by inject()

    private var _state = MutableStateFlow(SignInUiState())
    override val state = _state.asStateFlow()

    override fun onUp() {
        if (!state.value.isLoading) {
            navigateUp()
        }
    }

    private val callback = BackCallback { onUp() }

    init {
        backHandler.register(callback)
    }

    override fun signIn(email: String, password: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = signInUseCase(email, password)

            _state.value = _state.value.copy(isLoading = false)

            if (result is Ok) {
                withContext(Dispatchers.Main) { navigateToHome() }
            } else {
                print(result.unwrapError())
            }
        }
    }
}

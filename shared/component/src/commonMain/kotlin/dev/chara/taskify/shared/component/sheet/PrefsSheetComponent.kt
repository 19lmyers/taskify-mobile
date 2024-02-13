package dev.chara.taskify.shared.component.sheet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.domain.use_case.data.category_prefs.ReorderCategoriesUseCase
import dev.chara.taskify.shared.domain.use_case.data.category_prefs.UpdateCategoryPrefsUseCase
import dev.chara.taskify.shared.domain.use_case.home.GetReorderInfoUseCase
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableCategoryPrefs
import dev.chara.taskify.shared.model.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class PrefsSheetUiState(
    val isLoading: Boolean = false,
    val workspace: Workspace? = null,
    val categories: List<Category> = emptyList(),
    val prefs: List<CategoryPrefs> = emptyList()
)

interface PrefsSheetComponent {
    val state: StateFlow<PrefsSheetUiState>

    fun createPrefs(categoryId: Id)

    fun updatePrefs(categoryId: Id?, builder: (MutableCategoryPrefs) -> Unit)

    fun reorder(categoryId: Id, fromIndex: Int, toIndex: Int)

    fun cancel()
}

class DefaultPrefsSheetComponent(
    componentContext: ComponentContext,
    private val workspaceId: Id,
    private val dismiss: () -> Unit
) : PrefsSheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getReorderInfoUseCase: GetReorderInfoUseCase by inject()
    private val updateCategoryPrefsUseCase: UpdateCategoryPrefsUseCase by inject()
    private val reorderCategoriesUseCase: ReorderCategoriesUseCase by inject()

    private var _state = MutableStateFlow(PrefsSheetUiState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            getReorderInfoUseCase(workspaceId).collect {
                _state.value = PrefsSheetUiState(
                    isLoading = false,
                    workspace = it.workspace,
                    categories = it.categories,
                    prefs = it.prefs
                )
            }
        }
    }

    override fun cancel() = dismiss()

    override fun createPrefs(categoryId: Id) {
        coroutineScope.launch {
            updateCategoryPrefsUseCase(workspaceId, categoryId) {}
        }
    }

    override fun updatePrefs(categoryId: Id?, builder: (MutableCategoryPrefs) -> Unit) {
        coroutineScope.launch {
            updateCategoryPrefsUseCase(workspaceId, categoryId, builder)
        }
    }

    override fun reorder(categoryId: Id, fromIndex: Int, toIndex: Int) {
        coroutineScope.launch {
            reorderCategoriesUseCase(workspaceId, categoryId, fromIndex, toIndex)
        }
    }
}
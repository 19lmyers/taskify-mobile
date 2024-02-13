package dev.chara.taskify.shared.component.sheet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.chara.taskify.shared.domain.use_case.data.category.CreateCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.DeleteCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.GetCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.UpdateCategoryUseCase
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class CategorySheetUiState(
    val isLoading: Boolean = false,
    val category: Category? = null
)

interface CategorySheetComponent {
    val state: StateFlow<CategorySheetUiState>

    fun cancel()

    fun save(builder: (MutableCategory) -> Unit)

    fun delete()
}

class CreateCategoryComponent(
    componentContext: ComponentContext,
    private val workspaceId: Id,
    private val dismiss: () -> Unit
) : CategorySheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val createCategoryUseCase: CreateCategoryUseCase by inject()

    private var _state = MutableStateFlow(CategorySheetUiState())
    override val state = _state.asStateFlow()

    override fun cancel() = dismiss()

    override fun save(builder: (MutableCategory) -> Unit) {
        coroutineScope.launch {
            createCategoryUseCase(workspaceId, builder)
            dismiss()
        }
    }

    override fun delete() = Unit
}

class UpdateCategoryComponent(
    componentContext: ComponentContext,
    private val categoryId: Id,
    private val dismiss: (Boolean) -> Unit
) : CategorySheetComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    private val getCategoryUseCase: GetCategoryUseCase by inject()
    private val updateCategoryUseCase: UpdateCategoryUseCase by inject()
    private val deleteCategoryUseCase: DeleteCategoryUseCase by inject()

    private var _state = MutableStateFlow(CategorySheetUiState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            getCategoryUseCase(categoryId).collect {
                _state.value = _state.value.copy(isLoading = false, category = it)
            }
        }
    }

    override fun cancel() = dismiss(false)

    override fun save(builder: (MutableCategory) -> Unit) {
        coroutineScope.launch {
            updateCategoryUseCase(categoryId, builder)
            dismiss(false)
        }
    }

    override fun delete() {
        coroutineScope.launch {
            deleteCategoryUseCase(categoryId)
            dismiss(true)
        }
    }
}
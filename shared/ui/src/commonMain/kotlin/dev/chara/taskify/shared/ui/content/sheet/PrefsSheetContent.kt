package dev.chara.taskify.shared.ui.content.sheet

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.component.sheet.PrefsSheetComponent
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.MutableCategoryPrefs
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.harmonizeWithParent
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrefsSheetContent(component: PrefsSheetComponent) {
    val state by component.state.collectAsState()

    if (state.isLoading) {
        return
    }

    var reorderableList = remember { state.categories.toMutableStateList() }

    val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
        val fromIndex = from.index - 2
        val toIndex = to.index - 2

        reorderableList.getOrNull(fromIndex)?.id?.let {
            reorderableList = reorderableList.apply { add(toIndex, removeAt(fromIndex)) }
            component.reorder(it, fromIndex, toIndex)
        }
    }, canDragOver = { position, _ ->
        position.index >= 2
    })

    SideEffect {
        // Ensure prefs exist before reordering
        for (category in state.categories) {
            if (state.prefs.firstOrNull { it.categoryId == category.id } == null) {
                component.createPrefs(category.id)
            }
        }

        reorderableList.apply {
            clear()
            addAll(state.categories)
        }
    }

    ColorTheme(seed = state.workspace?.color?.harmonizeWithParent() ?: LocalSeedColor.current) {
        ModalBottomSheet(
            onDismissRequest = {
                component.cancel()
            },
            dragHandle = null,
            windowInsets = WindowInsets.ime,
            properties = ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false)
        ) {
            Column(
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp).windowInsetsPadding(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.Bottom
                    )
                )
            ) {
                LazyColumn(
                    state = reorderableState.listState,
                    modifier = Modifier.reorderable(reorderableState)
                ) {
                    item {
                        Text(
                            "Customize categories",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                        )
                    }

                    item {
                        val prefs = state.prefs.firstOrNull { it.categoryId == null }
                        CategoryPrefsItem(null, null, prefs) { id, builder ->
                            component.updatePrefs(id, builder)
                        }
                    }

                    items(reorderableList, { it.id.hexString }) { category ->
                        val prefs = state.prefs.firstOrNull { it.categoryId == category.id }
                        ReorderableItem(
                            reorderableState, key = category.id.hexString
                        ) { isDragging ->
                            CategoryPrefsItem(
                                category.id,
                                category.name,
                                prefs,
                                modifier = Modifier.animateItemPlacement()
                                    .detectReorderAfterLongPress(reorderableState),
                                isDragging = isDragging
                            ) { id, builder ->
                                component.updatePrefs(id, builder)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPrefsItem(
    categoryId: Id?,
    categoryName: String?,
    prefs: CategoryPrefs?,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    onUpdate: ((Id?, (MutableCategoryPrefs) -> Unit) -> Unit),
) {
    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

    var showTopTasksMenu by remember { mutableStateOf(false) }

    Surface(shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.padding(horizontal = 16.dp),
        tonalElevation = elevation,
        shadowElevation = elevation,
        onClick = {
            showTopTasksMenu = true
        }) {
        ListItem(headlineContent = {
            Text(categoryName ?: "Uncategorized")
        }, leadingContent = {
            if (categoryId != null) {
                Icon(Icons.Filled.Reorder, contentDescription = null)
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }, trailingContent = {
            Box {
                val count = prefs?.topTasksCount?.let {
                    if (it == 0) "None" else it.toString()
                } ?: "5"
                Text(count)

                DropdownMenu(expanded = showTopTasksMenu, onDismissRequest = {
                    showTopTasksMenu = false
                }) {
                    DropdownMenuItem(text = {
                        Text("None")
                    }, onClick = {
                        onUpdate(categoryId) {
                            it.topTasksCount = 0
                        }
                        showTopTasksMenu = false
                    })
                    DropdownMenuItem(text = {
                        Text("1")
                    }, onClick = {
                        onUpdate(categoryId) {
                            it.topTasksCount = 1
                        }
                        showTopTasksMenu = false
                    })
                    DropdownMenuItem(text = {
                        Text("3")
                    }, onClick = {
                        onUpdate(categoryId) {
                            it.topTasksCount = 3
                        }
                        showTopTasksMenu = false
                    })
                    DropdownMenuItem(text = {
                        Text("5")
                    }, onClick = {
                        onUpdate(categoryId) {
                            it.topTasksCount = 5
                        }
                        showTopTasksMenu = false
                    })
                    DropdownMenuItem(text = {
                        Text("10")
                    }, onClick = {
                        onUpdate(categoryId) {
                            it.topTasksCount = 10
                        }
                        showTopTasksMenu = false
                    })
                }
            }
        })
    }
}
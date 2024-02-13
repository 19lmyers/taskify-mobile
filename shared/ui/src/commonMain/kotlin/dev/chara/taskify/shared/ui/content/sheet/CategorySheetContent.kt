package dev.chara.taskify.shared.ui.content.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.component.sheet.CategorySheetComponent
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.model.SeedColor
import dev.chara.taskify.shared.ui.component.ColorSwatch
import dev.chara.taskify.shared.ui.ext.friendlyName
import dev.chara.taskify.shared.ui.ext.icon
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.blendWithParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySheetContent(component: CategorySheetComponent) {
    val state by component.state.collectAsState()

    if (state.isLoading) {
        return
    }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var name by rememberSaveable(state.category) { mutableStateOf(state.category?.name ?: "") }
    var color by rememberSaveable(state.category) { mutableStateOf(state.category?.color) }
    var classifier by rememberSaveable(state.category) { mutableStateOf(state.category?.classifier) }

    val sheetState = rememberModalBottomSheetState { value ->
        if (value == SheetValue.Hidden && (name.isNotBlank() && name != state.category?.name) || color != state.category?.color || classifier != state.category?.classifier) {
            showDiscardDialog = true
            false
        } else {
            true
        }
    }

    var showClassifierSelector by remember { mutableStateOf(false) }

    ColorTheme(
        seed = color?.blendWithParent() ?: LocalSeedColor.current
    ) {
        if (showDiscardDialog) {
            AlertDialog(onDismissRequest = {}, title = {
                Text(if (state.category == null) "Discard draft?" else "Discard changes?")
            }, confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    component.cancel()
                }) {
                    Text("Discard")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                }) {
                    Text("Cancel")
                }
            })
        }

        if (showDeleteDialog) {
            AlertDialog(onDismissRequest = {}, title = {
                Text("Delete category?")
            }, text = {
                Text("Tasks in this category will NOT be deleted")
            }, confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    component.delete()
                }) {
                    Text("Delete")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text("Cancel")
                }
            })
        }

        ModalBottomSheet(
            sheetState = sheetState,
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
                Text(
                    if (state.category == null) "New category" else "Edit category",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                    label = { Text(text = "Name") },
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    )
                )

                val outlineColor = MaterialTheme.colorScheme.outline
                val selectionColor = MaterialTheme.colorScheme.primary

                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    item {
                        ColorSwatch(
                            seed = null,
                            outline = outlineColor,
                            selection = selectionColor,
                            selected = color == null
                        ) {
                            color = null
                        }
                    }

                    items(items = SeedColor.entries.toTypedArray(), key = { it.name }) { swatch ->
                        ColorSwatch(
                            seed = swatch,
                            outline = outlineColor,
                            selection = selectionColor,
                            selected = color == swatch
                        ) {
                            color = swatch
                        }
                    }
                }

                Box {
                    ListItem(modifier = Modifier.clickable {
                        showClassifierSelector = true
                    }, headlineContent = {
                        Text(classifier.friendlyName)
                    }, leadingContent = {
                        Icon(classifier.icon, contentDescription = null)
                    })

                    Box(modifier = Modifier.padding(16.dp)) {
                        DropdownMenu(
                            expanded = showClassifierSelector,
                            onDismissRequest = { showClassifierSelector = false },
                        ) {
                            DropdownMenuItem(onClick = {
                                classifier = null
                                showClassifierSelector = false
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = null)
                                Spacer(modifier = Modifier.padding(8.dp))
                                Text("No grouping")
                            }
                            for (type in Category.Classifier.entries) {
                                DropdownMenuItem(onClick = {
                                    classifier = type
                                    showClassifierSelector = false
                                }) {
                                    Icon(type.icon, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(8.dp))
                                    Text(type.friendlyName)
                                }
                            }
                        }
                    }
                }

                Row {
                    if (state.category != null) {
                        OutlinedButton(modifier = Modifier.padding(
                            horizontal = 16.dp, vertical = 8.dp
                        ), onClick = {
                            showDeleteDialog = true
                        }) {
                            Text("Delete", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    FilledTonalButton(modifier = Modifier.padding(
                        horizontal = 16.dp, vertical = 8.dp
                    ), enabled = name.isNotBlank(), onClick = {
                        component.save {
                            it.name = name
                            it.color = color
                            it.classifier = classifier
                        }
                    }) {
                        Text("Save", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
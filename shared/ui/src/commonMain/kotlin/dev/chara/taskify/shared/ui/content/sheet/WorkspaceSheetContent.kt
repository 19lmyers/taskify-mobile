package dev.chara.taskify.shared.ui.content.sheet

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.component.sheet.WorkspaceSheetComponent
import dev.chara.taskify.shared.model.SeedColor
import dev.chara.taskify.shared.ui.component.ColorSwatch
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.harmonizeWithParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSheetContent(component: WorkspaceSheetComponent) {
    val state by component.state.collectAsState()

    if (state.isLoading) {
        return
    }

    if (state.creationInProgress) {
        AlertDialog(onDismissRequest = {}, title = {
            Text("Creating workspace")
        }, text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }, confirmButton = {})
        return
    }

    if (state.deletionInProgress) {
        AlertDialog(onDismissRequest = {}, title = {
            Text("Deleting workspace")
        }, text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }, confirmButton = {})
        return
    }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var name by rememberSaveable(state.workspace) { mutableStateOf(state.workspace?.name ?: "") }
    var color by rememberSaveable(state.workspace) { mutableStateOf(state.workspace?.color) }

    val sheetState = rememberModalBottomSheetState { value ->
        if (value == SheetValue.Hidden && (name != (state.workspace?.name
                ?: "") || color != state.workspace?.color)
        ) {
            showDiscardDialog = true
            false
        } else {
            true
        }
    }

    ColorTheme(seed = color?.harmonizeWithParent() ?: LocalSeedColor.current) {
        if (showDiscardDialog) {
            AlertDialog(onDismissRequest = {}, title = {
                Text(if (state.workspace == null) "Discard draft?" else "Discard changes?")
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
                Text("Delete workspace?")
            }, text = {
                Text("All categories and tasks in this workspace will be permanently deleted")
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
                    if (state.workspace == null) "New workspace" else "Edit workspace",
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

                Row {
                    if (state.workspace != null && state.current?.userId == state.workspace?.ownerId) {
                        OutlinedButton(modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                            onClick = {
                                showDeleteDialog = true
                            }) {
                            Text("Delete", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    FilledTonalButton(modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                        enabled = name.isNotBlank(),
                        onClick = {
                            component.save {
                                it.name = name
                                it.color = color
                            }
                        }) {
                        Text("Save", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
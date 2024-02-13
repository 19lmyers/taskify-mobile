package dev.chara.taskify.shared.ui.content.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.component.sheet.TaskSheetComponent
import dev.chara.taskify.shared.domain.util.formatDefault
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.ui.component.ProfilePhoto
import dev.chara.taskify.shared.ui.component.SelectedProfilePhoto
import dev.chara.taskify.shared.ui.content.dialog.DateTimePickerDialog
import dev.chara.taskify.shared.ui.ext.idStateSaver
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.blendWithParent
import kotlinx.datetime.LocalDateTime

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class
)
@Composable
fun TaskSheetContent(component: TaskSheetComponent) {
    val state by component.state.collectAsState()

    if (state.isLoading) {
        return
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    if (showDiscardDialog) {
        AlertDialog(onDismissRequest = {}, title = {
            Text("Discard draft?")
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

    var name by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable(saver = idStateSaver) { mutableStateOf(state.initialCategory) }
    var status by rememberSaveable { mutableStateOf(Task.Status.NotStarted) }
    var scheduledAt by rememberSaveable { mutableStateOf<LocalDateTime?>(null) }

    val assignedTo = remember { mutableStateListOf<String>() }

    val category by derivedStateOf {
        state.categories.firstOrNull { categoryId == it.id }
    }

    var showDateTimePickerDialog by rememberSaveable { mutableStateOf(false) }

    if (showDateTimePickerDialog) {
        DateTimePickerDialog(onDismiss = {
            showDateTimePickerDialog = false
        }, onConfirm = {
            status = Task.Status.NotStarted
            scheduledAt = it
            showDateTimePickerDialog = false
            if (assignedTo.isEmpty()) {
                assignedTo.add(state.currentUser!!.userId)
            }
        })
    }

    var showAssignmentDialog by rememberSaveable { mutableStateOf(false) }

    if (showAssignmentDialog) {
        val assignedToDialog =
            remember(showAssignmentDialog) { mutableStateListOf(*assignedTo.toTypedArray()) }

        AlertDialog(onDismissRequest = {
            showAssignmentDialog = false
        }, title = {
            Text("Assign to")
        }, text = {
            Column {
                state.members.forEach { member ->
                    ListItem(
                        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge).clickable {
                            if (assignedToDialog.contains(member.userId)) {
                                assignedToDialog.remove(member.userId)
                            } else {
                                assignedToDialog.add(member.userId)
                            }
                        },
                        leadingContent = {
                            if (assignedToDialog.contains(member.userId)) {
                                SelectedProfilePhoto()
                            } else {
                                ProfilePhoto(member)
                            }
                        },
                        headlineContent = {
                            Text(member.email)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                assignedTo.clear()
                assignedTo.addAll(assignedToDialog)
                showAssignmentDialog = false
            }) {
                Text("OK")
            }
        }, dismissButton = {
            TextButton(onClick = {
                showAssignmentDialog = false
            }) {
                Text("Cancel")
            }
        })
    }

    val sheetState = rememberModalBottomSheetState { value ->
        if (value == SheetValue.Hidden && name.isNotBlank()) {
            showDiscardDialog = true
            false
        } else {
            true
        }
    }

    var showCategorySelector by rememberSaveable { mutableStateOf(false) }
    var showScheduleSelector by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            component.cancel()
        },
        dragHandle = null,
        windowInsets = WindowInsets.ime,
        properties = ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false)
    ) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        ) {
            BasicTextField(
                modifier = Modifier.padding(16.dp).fillMaxWidth()
                    .focusRequester(focusRequester),
                value = name,
                onValueChange = { value: String ->
                    name = value
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (name.isBlank()) {
                        Text(
                            text = "New task",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    innerTextField()
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (name.isNotBlank() && !state.creationInProgress) {
                            component.save(
                                categoryId = categoryId,
                                classifierType = category?.classifier,
                                name = name
                            ) {
                                if (it.scheduledAt != scheduledAt) {
                                    it.reminderFired = false
                                }

                                it.name = name.trim()
                                it.status = status
                                it.scheduledAt = scheduledAt
                                it.assignedTo = assignedTo
                            }
                        }
                    }),
                enabled = !state.creationInProgress
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                FlowRow(modifier = Modifier.weight(1f)) {
                    if (state.categories.isNotEmpty()) {
                        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                            if (category == null) {
                                InputChip(selected = false, leadingIcon = {
                                    Icon(Icons.Filled.Category, contentDescription = null)
                                }, label = {
                                    Text("Category")
                                }, onClick = {
                                    if (!state.creationInProgress) {
                                        showCategorySelector = true
                                    }
                                }, border = null
                                )
                            } else {
                                ColorTheme(
                                    seed = category?.color?.blendWithParent()
                                        ?: LocalSeedColor.current
                                ) {
                                    InputChip(selected = true, leadingIcon = {
                                        Icon(Icons.Filled.Category, contentDescription = null)
                                    }, label = {
                                        Text(category?.name ?: "Uncategorized")
                                    }, onClick = {
                                        if (!state.creationInProgress) {
                                            showCategorySelector = true
                                        }
                                    }, border = null)
                                }
                            }

                            DropdownMenu(expanded = showCategorySelector,
                                onDismissRequest = { showCategorySelector = false }) {
                                DropdownMenuItem(onClick = {
                                    categoryId = null
                                    showCategorySelector = false
                                }, text = {
                                    Text("None")
                                }, leadingIcon = {
                                    Icon(Icons.Filled.Close, contentDescription = null)
                                })
                                for (c in state.categories) {
                                    DropdownMenuItem(onClick = {
                                        if (!state.creationInProgress) {
                                            categoryId = c.id
                                            showCategorySelector = false
                                        }
                                    }, text = {
                                        Text(c.name)
                                    }, leadingIcon = {
                                        Icon(Icons.Filled.Category, contentDescription = null)
                                    })
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                        if (status == Task.Status.InProgress) {
                            InputChip(selected = true, leadingIcon = {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            }, label = {
                                Text("Now")
                            }, onClick = {
                                if (!state.creationInProgress) {
                                    showScheduleSelector = true
                                }
                            }, border = null)
                        } else if (scheduledAt != null) {
                            InputChip(selected = true, leadingIcon = {
                                Icon(Icons.Filled.Schedule, contentDescription = null)
                            }, label = {
                                Text(scheduledAt!!.formatDefault())
                            }, onClick = {
                                if (!state.creationInProgress) {
                                    showScheduleSelector = true
                                }
                            }, border = null)
                        } else {
                            InputChip(selected = false, leadingIcon = {
                                Icon(Icons.Filled.Event, contentDescription = null)
                            }, label = {
                                Text("Later")
                            }, onClick = {
                                if (!state.creationInProgress) {
                                    showScheduleSelector = true
                                }
                            }, border = null)
                        }

                        DropdownMenu(expanded = showScheduleSelector,
                            onDismissRequest = { showScheduleSelector = false }) {
                            DropdownMenuItem(onClick = {
                                if (!state.creationInProgress) {
                                    status = Task.Status.InProgress
                                    scheduledAt = null
                                    showScheduleSelector = false
                                }
                            }, text = {
                                Text("Now")
                            }, leadingIcon = {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            })
                            DropdownMenuItem(onClick = {
                                if (!state.creationInProgress) {
                                    status = Task.Status.NotStarted
                                    scheduledAt = null
                                    showScheduleSelector = false
                                }
                            }, text = {
                                Text("Later")
                            }, leadingIcon = {
                                Icon(Icons.Filled.Event, contentDescription = null)
                            })
                            DropdownMenuItem(onClick = {
                                if (!state.creationInProgress) {
                                    showDateTimePickerDialog = true
                                    showScheduleSelector = false
                                }
                            }, text = {
                                Text("Schedule")
                            }, leadingIcon = {
                                Icon(Icons.Filled.Schedule, contentDescription = null)
                            })
                        }
                    }

                    if (assignedTo.isEmpty()) {
                        InputChip(selected = false, leadingIcon = {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        }, label = {
                            Text("Assign")
                        }, onClick = {
                            showAssignmentDialog = true
                        }, border = null)
                    } else if (assignedTo.size > 1) {
                        InputChip(selected = true, leadingIcon = {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        }, label = {
                            Text("${assignedTo.size} members")
                        }, onClick = {
                            showAssignmentDialog = true
                        }, border = null)
                    } else {
                        val member = state.members.first {
                            it.userId == assignedTo.first()
                        }

                        InputChip(selected = true, leadingIcon = {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        }, label = {
                            Text(member.email)
                        }, onClick = {
                            showAssignmentDialog = true
                        }, border = null)
                    }

                }
                if (state.creationInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Bottom)
                            .padding(16.dp)
                            .size(24.dp)
                    )
                } else {
                    TextButton(
                        enabled = name.isNotBlank() && !state.creationInProgress,
                        modifier = Modifier.align(Alignment.Bottom),
                        onClick = {
                            component.save(
                                categoryId = categoryId,
                                classifierType = category?.classifier,
                                name = name
                            ) {
                                if (it.scheduledAt != scheduledAt) {
                                    it.reminderFired = false
                                }

                                it.name = name.trim()
                                it.status = status
                                it.scheduledAt = scheduledAt
                                it.assignedTo = assignedTo
                            }
                        }
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
}
package dev.chara.taskify.shared.ui.content.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.arkivanov.essenty.backhandler.BackCallback
import dev.chara.taskify.shared.component.details.TaskDetailsComponent
import dev.chara.taskify.shared.domain.isOverdue
import dev.chara.taskify.shared.domain.util.formatDefault
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.ui.component.ProfilePhoto
import dev.chara.taskify.shared.ui.component.SelectedProfilePhoto
import dev.chara.taskify.shared.ui.content.dialog.DateTimePickerDialog
import dev.chara.taskify.shared.ui.ext.idStateSaver
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.blendWith
import dev.chara.taskify.shared.ui.theme.toColor
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailsContent(component: TaskDetailsComponent) {
    val state by component.state.collectAsState()

    var name by rememberSaveable(state.task) { mutableStateOf(state.task?.name ?: "") }

    var categoryId by rememberSaveable(
        state.task, saver = idStateSaver
    ) { mutableStateOf(state.task?.categoryId) }

    val category by derivedStateOf {
        state.categories.firstOrNull { it.id == categoryId }
    }

    var group by remember(state.classification, state.task) {
        mutableStateOf(state.classification ?: state.task?.group)
    }

    var status by rememberSaveable(state.task) {
        mutableStateOf(state.task?.status ?: Task.Status.NotStarted)
    }

    var scheduledAt by rememberSaveable(state.task) { mutableStateOf(state.task?.scheduledAt) }

    val assignedTo = remember { mutableStateListOf<String>() }

    LaunchedEffect(state.task?.assignedTo) {
        assignedTo.clear()
        assignedTo.addAll(state.task?.assignedTo ?: emptyList())
    }

    val modified by derivedStateOf {
        name != state.task?.name || categoryId != state.task?.categoryId || group != state.task?.group
                || status != state.task?.status || scheduledAt != state.task?.scheduledAt
                || !assignedTo.toTypedArray().contentEquals(state.task?.assignedTo?.toTypedArray())
    }

    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showDateTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showAssignmentDialog by rememberSaveable { mutableStateOf(false) }

    var showOverflow by rememberSaveable { mutableStateOf(false) }
    var showCategorySelector by rememberSaveable { mutableStateOf(false) }
    var showScheduleSelector by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val callback = BackCallback(isEnabled = modified) {
        showDiscardDialog = true
    }

    DisposableEffect(modified) {
        component.backHandler.register(callback)

        onDispose {
            component.backHandler.unregister(callback)
        }
    }

    ColorTheme(
        seed = category?.color?.blendWith(
            state.workspace?.color?.toColor() ?: LocalSeedColor.current
        ) ?: state.workspace?.color?.toColor() ?: LocalSeedColor.current
    ) {
        if (showDiscardDialog) {
            AlertDialog(onDismissRequest = {}, title = {
                Text("Discard changes?")
            }, confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    component.onUp()
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
            AlertDialog(onDismissRequest = {
                showDeleteDialog = false
            }, title = {
                Text("Delete task?")
            }, text = {
                Text("This task will be permanently deleted")
            }, confirmButton = {
                TextButton(onClick = {
                    component.delete()
                    showDeleteDialog = false
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

        Scaffold(topBar = {
            CenterAlignedTopAppBar(title = {
                if (state.workspace != null) {
                    Text("Edit task")
                }
            }, navigationIcon = {
                IconButton(onClick = {
                    if (modified) {
                        showDiscardDialog = true
                    } else {
                        component.onUp()
                    }
                }) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                }
            }, actions = {
                Box {
                    IconButton(onClick = { showOverflow = true }) {
                        Icon(
                            Icons.Filled.MoreVert, contentDescription = "Show more options"
                        )
                    }

                    DropdownMenu(expanded = showOverflow, onDismissRequest = {
                        showOverflow = false
                    }) {
                        DropdownMenuItem(onClick = {
                            showDeleteDialog = true
                            showOverflow = false
                        }, text = {
                            Text("Delete")
                        }, leadingIcon = {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        })
                    }
                }
            }, scrollBehavior = scrollBehavior
            )
        }, bottomBar = {
            BottomAppBar(modifier = Modifier.padding(WindowInsets.ime.asPaddingValues())) {
                Spacer(Modifier.weight(1f, true))

                Button(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    onClick = {
                        component.save(categoryId) {
                            if (it.scheduledAt != scheduledAt) {
                                it.reminderFired = false
                            }

                            it.name = name
                            it.group = group
                            it.status = status
                            it.scheduledAt = scheduledAt
                            it.assignedTo = assignedTo
                        }
                    },
                    enabled = modified && name.isNotBlank() && (group == null || group?.isNotEmpty() == true)
                ) {
                    Text(text = "Save")
                }
            }
        }) { innerPadding ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(scrollState).padding(innerPadding)
                ) {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 5.dp)) {
                            Checkbox(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                checked = status == Task.Status.Complete,
                                onCheckedChange = { checked ->
                                    status = if (checked) {
                                        Task.Status.Complete
                                    } else {
                                        Task.Status.NotStarted
                                    }
                                    scheduledAt = null
                                },
                                colors = CheckboxDefaults.colors(
                                    uncheckedColor = if (state.task?.scheduledAt?.isOverdue() == true) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            )

                            BasicTextField(
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .padding(vertical = 16.dp, horizontal = 2.dp).fillMaxWidth(),
                                value = name,
                                onValueChange = {
                                    name = it
                                },
                                singleLine = false,
                                textStyle = if (status == Task.Status.InProgress || scheduledAt?.isOverdue() == true) {
                                    MaterialTheme.typography.titleMedium
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                }.copy(
                                    color = if (status == Task.Status.InProgress) {
                                        MaterialTheme.colorScheme.primary
                                    } else if (scheduledAt?.isOverdue() == true) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onBackground
                                    }
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (name.isEmpty()) {
                                        Text(
                                            text = "Enter task",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    innerTextField()
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Text(
                            "Properties",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                        )
                        Box {
                            ListItem(modifier = Modifier.clickable {
                                showCategorySelector = true
                            }, headlineContent = {
                                Text(category?.name ?: "Uncategorized")
                            }, leadingContent = {
                                Icon(Icons.Filled.Category, contentDescription = null)
                            }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))

                            Box(modifier = Modifier.padding(16.dp)) {
                                DropdownMenu(
                                    expanded = showCategorySelector,
                                    onDismissRequest = { showCategorySelector = false },
                                ) {
                                    DropdownMenuItem(onClick = {
                                        categoryId = null
                                        showCategorySelector = false
                                    }, leadingIcon = {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                    }, text = {
                                        Text("None")
                                    })
                                    for (c in state.categories) {
                                        DropdownMenuItem(onClick = {
                                            categoryId = c.id
                                            showCategorySelector = false
                                        }, leadingIcon = {
                                            Icon(Icons.Filled.Category, contentDescription = null)
                                        }, text = {
                                            Text(c.name)
                                        })
                                    }
                                }
                            }
                        }

                        ListItem(
                            headlineContent = {
                                BasicTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = group ?: "",
                                    onValueChange = {
                                        group = it.ifEmpty { null }
                                    },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                                    singleLine = true,
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { innerTextField ->
                                        if (group.isNullOrEmpty()) {
                                            Text(
                                                text = "Ungrouped",
                                                color = MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                        }
                                        innerTextField()
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.GroupWork,
                                    contentDescription = null
                                )
                            },
                            trailingContent = {
                                if (category?.classifier != null) {
                                    IconButton(onClick = {
                                        component.reclassify(
                                            categoryId,
                                            name,
                                            category!!.classifier
                                        )
                                    }) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null)
                                    }
                                }
                            }, colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        Box {
                            ListItem(modifier = Modifier.clickable {
                                showScheduleSelector = true
                            }, headlineContent = {
                                if (status == Task.Status.InProgress) {
                                    Text("Now")
                                } else if (scheduledAt != null) {
                                    Text(scheduledAt!!.formatDefault())
                                } else {
                                    Text("Later")
                                }
                            }, leadingContent = {
                                if (status == Task.Status.InProgress) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                } else if (scheduledAt != null) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null)
                                } else {
                                    Icon(Icons.Filled.Event, contentDescription = null)
                                }
                            }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))

                            Box(modifier = Modifier.padding(16.dp)) {
                                DropdownMenu(
                                    expanded = showScheduleSelector,
                                    onDismissRequest = { showScheduleSelector = false },
                                ) {
                                    DropdownMenuItem(onClick = {
                                        status = Task.Status.InProgress
                                        scheduledAt = null
                                        showScheduleSelector = false
                                    }, leadingIcon = {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    }, text = {
                                        Text("Now")
                                    })
                                    DropdownMenuItem(onClick = {
                                        status = Task.Status.NotStarted
                                        scheduledAt = null
                                        showScheduleSelector = false
                                    }, leadingIcon = {
                                        Icon(Icons.Filled.Event, contentDescription = null)
                                    }, text = {
                                        Text("Later")
                                    })
                                    DropdownMenuItem(onClick = {
                                        showDateTimePickerDialog = true
                                        showScheduleSelector = false
                                    }, leadingIcon = {
                                        Icon(Icons.Filled.Schedule, contentDescription = null)
                                    }, text = {
                                        Text("Schedule")
                                    })
                                }
                            }
                        }

                        ListItem(headlineContent = {
                            if (assignedTo.isEmpty()) {
                                Text("Unassigned")
                            } else if (assignedTo.size > 1) {
                                Text("${assignedTo.size} members")
                            } else {
                                val member = state.members.first {
                                    it.userId == assignedTo.first()
                                }

                                Text(member.email)
                            }
                        }, leadingContent = {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        }, trailingContent = {
                            if (assignedTo.isNotEmpty()) {
                                FlowRow {
                                    for (profile in state.members.filter { assignedTo.contains(it.userId) }) {
                                        ProfilePhoto(profile)
                                    }
                                }
                            }
                        }, modifier = Modifier.clickable {
                            showAssignmentDialog = true
                        }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Text(
                            "Task info",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                        )
                        ListItem(headlineContent = {
                            Text(
                                "Last modified at",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }, leadingContent = {
                            Icon(
                                Icons.Filled.EditCalendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }, trailingContent = {
                            Text(
                                state.task?.lastModifiedAt?.toLocalDateTime(TimeZone.currentSystemDefault())
                                    ?.formatDefault() ?: "Unknown",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))

                        ListItem(headlineContent = {
                            Text(
                                "Last modified by",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }, leadingContent = {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }, trailingContent = {
                            Text(
                                state.lastModifiedBy?.email ?: "Unknown",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                    }
                }
            }
        }
    }
}
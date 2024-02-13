package dev.chara.taskify.shared.ui.content.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chara.taskify.shared.component.details.CategoryDetailsComponent
import dev.chara.taskify.shared.domain.suggest
import dev.chara.taskify.shared.model.CategoryPrefs
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.ui.component.ProfilePhoto
import dev.chara.taskify.shared.ui.component.SelectedProfilePhoto
import dev.chara.taskify.shared.ui.component.Task
import dev.chara.taskify.shared.ui.component.TaskActions
import dev.chara.taskify.shared.ui.content.dialog.DateTimePickerDialog
import dev.chara.taskify.shared.ui.content.sheet.CategorySheetContent
import dev.chara.taskify.shared.ui.content.sheet.TaskSheetContent
import dev.chara.taskify.shared.ui.ext.friendlyName
import dev.chara.taskify.shared.ui.ext.icon
import dev.chara.taskify.shared.ui.ext.idStateSaver
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.blendWith
import dev.chara.taskify.shared.ui.theme.toColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CategoryDetailsContent(component: CategoryDetailsComponent) {
    val state by component.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showActionMenuForTask by rememberSaveable(idStateSaver) { mutableStateOf<Id?>(null) }

    var showDeleteCompletedDialog by remember { mutableStateOf(false) }
    var showDateTimePickerDialogForTask by remember { mutableStateOf<Id?>(null) }
    var showAssignmentDialogForTask by remember { mutableStateOf<Id?>(null) }

    var showOverflow by rememberSaveable { mutableStateOf(false) }
    var showCompletedTasks by rememberSaveable { mutableStateOf(false) }
    var showSortModes by rememberSaveable { mutableStateOf(false) }

    ColorTheme(
        seed = state.category?.color?.blendWith(
            state.workspace?.color?.toColor() ?: LocalSeedColor.current
        ) ?: state.workspace?.color?.toColor() ?: LocalSeedColor.current
    ) {
        val sheet by component.sheet.subscribeAsState()
        sheet.child?.instance?.also { child ->
            when (child) {
                is CategoryDetailsComponent.Child.CategorySheet -> CategorySheetContent(child.component)
                is CategoryDetailsComponent.Child.TaskSheet -> TaskSheetContent(child.component)
            }
        }

        if (showDeleteCompletedDialog) {
            AlertDialog(onDismissRequest = {
                showDeleteCompletedDialog = false
            }, title = {
                Text("Delete all completed tasks?")
            }, text = {
                Text("All completed tasks in this category will be permanently deleted")
            }, confirmButton = {
                TextButton(onClick = {
                    component.deleteAllCompletedTasks()
                    showDeleteCompletedDialog = false
                }) {
                    Text("Delete")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    showDeleteCompletedDialog = false
                }) {
                    Text("Cancel")
                }
            })
        }

        if (showDateTimePickerDialogForTask != null) {
            DateTimePickerDialog(onDismiss = {
                showDateTimePickerDialogForTask = null
            }, onConfirm = { scheduledAt ->
                component.updateTask(showDateTimePickerDialogForTask!!) {
                    if (it.scheduledAt != scheduledAt) {
                        it.reminderFired = false
                    }

                    it.status = Task.Status.NotStarted
                    it.scheduledAt = scheduledAt

                    if (it.assignedTo.isEmpty()) {
                        it.assignedTo = listOf(state.currentUser!!.userId)
                    }
                }
                showDateTimePickerDialogForTask = null
            })
        }

        val assignmentDialog = @Composable { task: Task ->
            if (showAssignmentDialogForTask == task.id) {
                val assignedTo =
                    remember(showAssignmentDialogForTask) { mutableStateListOf(*task.assignedTo.toTypedArray()) }

                AlertDialog(onDismissRequest = {
                    showAssignmentDialogForTask = null
                }, title = {
                    Text("Assign to")
                }, text = {
                    Column {
                        state.members.forEach { member ->
                            ListItem(
                                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge).clickable {
                                    if (assignedTo.contains(member.userId)) {
                                        assignedTo.remove(member.userId)
                                    } else {
                                        assignedTo.add(member.userId)
                                    }
                                },
                                leadingContent = {
                                    if (assignedTo.contains(member.userId)) {
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
                        component.updateTask(task.id) {
                            it.assignedTo = assignedTo

                            if (it.assignedTo.isEmpty()) {
                                it.scheduledAt = null
                            }
                        }
                        showAssignmentDialogForTask = null
                    }) {
                        Text("OK")
                    }
                }, dismissButton = {
                    TextButton(onClick = {
                        showAssignmentDialogForTask = null
                    }) {
                        Text("Cancel")
                    }
                })
            }
        }

        Scaffold(topBar = {
            CenterAlignedTopAppBar(title = {
                if (state.workspace != null) {
                    Text(state.category?.name ?: "Uncategorized")
                }
            }, navigationIcon = {
                IconButton(onClick = {
                    component.onUp()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }, actions = {
                if (state.category != null) {
                    IconButton(onClick = {
                        component.showUpdateCategory()
                    }) {
                        Icon(
                            Icons.Filled.Edit, contentDescription = "Edit category"
                        )
                    }
                }
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
                            showDeleteCompletedDialog = true
                            showOverflow = false
                        }, text = {
                            Text("Delete all completed tasks")
                        }, leadingIcon = {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        })
                    }
                }
            }, scrollBehavior = scrollBehavior
            )
        }, bottomBar = {
            BottomAppBar(floatingActionButton = {
                FloatingActionButton(onClick = {
                    component.showCreateTask()
                }, elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()) {
                    Icon(Icons.Filled.Add, contentDescription = "New task")
                }
            }, actions = {
                Box {
                    TextButton(onClick = {
                        showSortModes = true
                    }) {
                        Icon(state.prefs?.sortType.icon, contentDescription = null)
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = state.prefs?.sortType.friendlyName)
                    }

                    DropdownMenu(expanded = showSortModes, onDismissRequest = {
                        showSortModes = false
                    }) {
                        for (mode in CategoryPrefs.SortType.entries) {
                            DropdownMenuItem(text = {
                                Text(text = mode.friendlyName)
                            }, onClick = {
                                component.updateCategoryPrefs {
                                    it.sortType = mode
                                }
                                showSortModes = false
                            }, leadingIcon = {
                                Icon(mode.icon, contentDescription = null)
                            })
                        }
                    }
                }

                TextButton(onClick = {
                    component.updateCategoryPrefs {
                        if (state.prefs?.sortDirection == CategoryPrefs.SortDirection.Ascending) {
                            it.sortDirection = CategoryPrefs.SortDirection.Descending
                        } else {
                            it.sortDirection = CategoryPrefs.SortDirection.Ascending
                        }
                    }
                }) {
                    Icon(state.prefs?.sortDirection.icon, contentDescription = "Sort order")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = state.prefs?.sortDirection.friendlyName)
                }
            })
        }) { contentPadding ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                val lazyListState = rememberLazyListState()

                val showGroups = remember { mutableStateMapOf<String?, Boolean>() }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = contentPadding,
                ) {
                    if (state.prefs?.sortType == CategoryPrefs.SortType.Group) {
                        val groups = state.currentTasks.map { it.group }.toSet().run {
                            if (state.prefs?.sortDirection == CategoryPrefs.SortDirection.Descending) {
                                sortedByDescending { it }
                            } else {
                                sortedBy { it }
                            }
                        }

                        for (group in groups) {
                            val groupTasks = state.currentTasks.filter { it.group == group }

                            item(key = "group/$group", contentType = "group") {
                                Surface(color = Color.Transparent,
                                    shape = MaterialTheme.shapes.extraLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(top = 12.dp),
                                    onClick = {
                                        showGroups[group] = !(showGroups[group] ?: true)
                                    }) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(
                                            horizontal = 12.dp, vertical = 6.dp
                                        )
                                    ) {
                                        Text(
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            text = "${group ?: "Ungrouped"} (${groupTasks.size})",
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Icon(
                                            if (showGroups[group] != false) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }

                            if (showGroups[group] != false) {
                                items(groupTasks,
                                    key = { "task/${it.id}" },
                                    contentType = { "task" }) { task ->

                                    assignmentDialog(task)

                                    Task(task,
                                        assignedTo = state.members.filter {
                                            task.assignedTo.contains(
                                                it.userId
                                            )
                                        },
                                        description = task.suggest().description,
                                        selected = showActionMenuForTask == task.id,
                                        onClick = {
                                            component.showTaskDetails(task.id)
                                        },
                                        onLongClick = { selected ->
                                            showActionMenuForTask = if (selected) {
                                                null
                                            } else {
                                                task.id
                                            }
                                        },
                                        onUpdate = {
                                            component.updateTask(task.id, it)
                                        }) {
                                        TaskActions(task = task, onStart = {
                                            showActionMenuForTask = null
                                            component.updateTask(task.id) {
                                                it.status = Task.Status.InProgress
                                                it.scheduledAt = null
                                            }
                                        }, onCancel = {
                                            showActionMenuForTask = null
                                            component.updateTask(task.id) {
                                                it.status = Task.Status.NotStarted
                                                it.scheduledAt = null
                                            }
                                        }, onSchedule = {
                                            showActionMenuForTask = null
                                            showDateTimePickerDialogForTask = task.id
                                        }, onAssign = {
                                            showActionMenuForTask = null
                                            showAssignmentDialogForTask = task.id
                                        })
                                    }
                                }
                            }
                        }
                    } else {
                        items(state.currentTasks,
                            key = { "task/${it.id}" },
                            contentType = { "task" }) { task ->

                            assignmentDialog(task)

                            Task(task,
                                assignedTo = state.members.filter { task.assignedTo.contains(it.userId) },
                                description = task.suggest().description,
                                selected = showActionMenuForTask == task.id,
                                onClick = {
                                    component.showTaskDetails(task.id)
                                },
                                onLongClick = { selected ->
                                    showActionMenuForTask = if (selected) {
                                        null
                                    } else {
                                        task.id
                                    }
                                },
                                onUpdate = {
                                    component.updateTask(task.id, it)
                                }) {
                                TaskActions(task = task, onStart = {
                                    showActionMenuForTask = null
                                    component.updateTask(task.id) {
                                        it.status = Task.Status.InProgress
                                        it.scheduledAt = null
                                    }
                                }, onCancel = {
                                    showActionMenuForTask = null
                                    component.updateTask(task.id) {
                                        it.status = Task.Status.NotStarted
                                        it.scheduledAt = null
                                    }
                                }, onSchedule = {
                                    showActionMenuForTask = null
                                    showDateTimePickerDialogForTask = task.id
                                }, onAssign = {
                                    showActionMenuForTask = null
                                    showAssignmentDialogForTask = task.id
                                })
                            }
                        }
                    }

                    if (state.currentTasks.isEmpty()) {
                        item(
                            key = "placeholder", contentType = "placeholder"
                        ) {
                            Box(
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            ) {
                                Text(
                                    text = "All tasks complete!", modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 6.dp
                                    ).align(Alignment.Center)
                                )
                            }
                        }
                    }

                    if (state.completedTasks.isNotEmpty()) {
                        item(key = "divider", contentType = "divider") {
                            Surface(color = Color.Transparent,
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                onClick = {
                                    showCompletedTasks = !showCompletedTasks
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        text = "Completed (${state.completedTasks.size})",
                                    )
                                    Spacer(Modifier.weight(1f))
                                    if (showCompletedTasks) {
                                        Icon(
                                            Icons.Filled.ExpandLess, contentDescription = null
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.ExpandMore, contentDescription = null
                                        )
                                    }
                                }
                            }
                        }

                        if (showCompletedTasks) {
                            items(state.completedTasks,
                                key = { "task/${it.id}" },
                                contentType = { "task" }) { task ->
                                assignmentDialog(task)

                                Task(task,
                                    assignedTo = state.members.filter { task.assignedTo.contains(it.userId) },
                                    selected = showActionMenuForTask == task.id,
                                    onClick = {
                                        component.showTaskDetails(task.id)
                                    },
                                    onLongClick = { selected ->
                                        showActionMenuForTask = if (selected) {
                                            null
                                        } else {
                                            task.id
                                        }
                                    },
                                    onUpdate = {
                                        component.updateTask(task.id, it)
                                    }) {
                                    TaskActions(task = task, onStart = {
                                        showActionMenuForTask = null
                                        component.updateTask(task.id) {
                                            it.status = Task.Status.InProgress
                                            it.scheduledAt = null
                                        }
                                    }, onCancel = {
                                        showActionMenuForTask = null
                                        component.updateTask(task.id) {
                                            it.status = Task.Status.NotStarted
                                            it.scheduledAt = null
                                        }
                                    }, onSchedule = {
                                        showActionMenuForTask = null
                                        showDateTimePickerDialogForTask = task.id
                                    }, onAssign = {
                                        showActionMenuForTask = null
                                        showAssignmentDialogForTask = task.id
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
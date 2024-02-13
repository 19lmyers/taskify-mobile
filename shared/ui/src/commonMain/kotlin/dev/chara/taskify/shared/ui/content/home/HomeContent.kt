package dev.chara.taskify.shared.ui.content.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chara.taskify.shared.component.home.HomeComponent
import dev.chara.taskify.shared.domain.suggest
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Permission
import dev.chara.taskify.shared.model.Task
import dev.chara.taskify.shared.ui.component.BackdropScaffold
import dev.chara.taskify.shared.ui.component.NewWorkspace
import dev.chara.taskify.shared.ui.component.ProfilePhoto
import dev.chara.taskify.shared.ui.component.SelectedProfilePhoto
import dev.chara.taskify.shared.ui.component.Task
import dev.chara.taskify.shared.ui.component.TaskActions
import dev.chara.taskify.shared.ui.component.WorkspaceActions
import dev.chara.taskify.shared.ui.component.WorkspacePreview
import dev.chara.taskify.shared.ui.content.dialog.DateTimePickerDialog
import dev.chara.taskify.shared.ui.content.sheet.CategorySheetContent
import dev.chara.taskify.shared.ui.content.sheet.JoinSheetContent
import dev.chara.taskify.shared.ui.content.sheet.PrefsSheetContent
import dev.chara.taskify.shared.ui.content.sheet.ShareSheetContent
import dev.chara.taskify.shared.ui.content.sheet.TaskSheetContent
import dev.chara.taskify.shared.ui.content.sheet.WorkspaceSheetContent
import dev.chara.taskify.shared.ui.ext.idStateSaver
import dev.chara.taskify.shared.ui.permission.rememberPermissionRequestState
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.blendWithParent
import dev.chara.taskify.shared.ui.theme.toColor
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class
)
@Composable
fun HomeContent(component: HomeComponent) {
    val coroutineScope = rememberCoroutineScope()

    val state by component.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)

    var showActionMenuForTask by rememberSaveable(saver = idStateSaver) { mutableStateOf(null) }

    var showOverflow by rememberSaveable { mutableStateOf(false) }

    val notificationRequest = rememberPermissionRequestState(Permission.Notifications)

    ColorTheme(seed = state.workspace?.color?.toColor() ?: LocalSeedColor.current) {
        val sheet by component.sheet.subscribeAsState()
        sheet.child?.instance?.also { child ->
            when (child) {
                is HomeComponent.Child.WorkspaceSheet -> WorkspaceSheetContent(child.component)
                is HomeComponent.Child.PrefsSheet -> PrefsSheetContent(child.component)
                is HomeComponent.Child.ShareSheet -> ShareSheetContent(child.component)
                is HomeComponent.Child.JoinSheet -> JoinSheetContent(child.component)
                is HomeComponent.Child.CategorySheet -> CategorySheetContent(child.component)
                is HomeComponent.Child.TaskSheet -> TaskSheetContent(child.component)
            }
        }

        var showDateTimePickerDialogForTask by remember { mutableStateOf<Id?>(null) }

        if (showDateTimePickerDialogForTask != null) {
            DateTimePickerDialog(onDismiss = {
                showDateTimePickerDialogForTask = null
            }, onConfirm = { scheduledAt ->
                component.updateTask(showDateTimePickerDialogForTask!!) {
                    it.status = Task.Status.NotStarted
                    it.scheduledAt = scheduledAt
                    if (it.assignedTo.isEmpty()) {
                        it.assignedTo = listOf(state.currentUser!!.userId)
                    }
                }
                showDateTimePickerDialogForTask = null
            })
        }

        var showAssignmentDialogForTask by rememberSaveable(saver = idStateSaver) {
            mutableStateOf(
                null
            )
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

        val lazyListState = rememberLazyListState()

        BackdropScaffold(scaffoldState = scaffoldState, appBarHeight = 64.dp, appBar = {
            CenterAlignedTopAppBar(
                title = {
                    AnimatedContent(targetState = scaffoldState.targetValue == BackdropValue.Revealed,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { isRevealed ->
                        if (isRevealed) {
                            Text("Workspaces")
                        } else {
                            Text(state.workspace?.name ?: "")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (scaffoldState.isRevealed) scaffoldState.conceal() else scaffoldState.reveal()
                        }
                    }) {
                        AnimatedContent(targetState = scaffoldState.targetValue == BackdropValue.Revealed,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }) { isRevealed ->
                            if (isRevealed) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Close workspace switcher"
                                )
                            } else {
                                Icon(Icons.Filled.Menu, contentDescription = "Switch workspaces")
                            }
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = {
                            showOverflow = true
                        }) {
                            ProfilePhoto(state.currentUser)
                        }

                        DropdownMenu(showOverflow, onDismissRequest = {
                            showOverflow = false
                        }) {
                            DropdownMenuItem(text = {
                                Text("Sign out")
                            }, leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            }, onClick = {
                                component.signOut()
                            })
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }, frontLayerContent = {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                Scaffold(
                    floatingActionButton = {
                        if (state.workspace != null) {
                            ExtendedFloatingActionButton(onClick = {
                                state.workspace?.id?.let { workspaceId ->
                                    component.showCreateTask(workspaceId)
                                }
                            }, icon = {
                                Icon(Icons.Filled.Add, contentDescription = null)
                            }, text = {
                                Text("New task")
                            }, expanded = !lazyListState.canScrollBackward)
                        }
                    }, containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    if (state.workspace == null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "No workspace selected", modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        val insets = WindowInsets.systemGestures.asPaddingValues()

                        LazyColumn(
                            state = lazyListState, modifier = Modifier.fillMaxSize()
                        ) {
                            if (!notificationRequest.isGranted.value && !notificationRequest.isHidden.value) {
                                item(key = "notification-request") {
                                    Card(
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                            .padding(top = 16.dp)
                                    ) {
                                        Text(
                                            "Enable notifications",
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                                .padding(top = 16.dp, bottom = 8.dp)
                                        )
                                        Text(
                                            "To send you reminders and updates on your tasks, Taskify requires permission to post notifications.",
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                                .padding(bottom = 8.dp)
                                        ) {
                                            TextButton(onClick = {
                                                coroutineScope.launch {
                                                    notificationRequest.hide()
                                                }
                                            }) {
                                                Text("Dismiss")
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            TextButton(onClick = {
                                                notificationRequest.launchRequest()
                                            }) {
                                                Text("Enable")
                                            }
                                        }
                                    }
                                }
                            }
                            item(key = "suggested/header") {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(top = 16.dp).fillMaxWidth()
                                ) {
                                    Text(
                                        text = "To Do", modifier = Modifier.padding(
                                            horizontal = 12.dp, vertical = 6.dp
                                        ), style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            if (state.suggestedTasks.isEmpty()) {
                                item(key = "suggested/placeholder") {
                                    Row(
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                                            .padding(12.dp).fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "No suggested tasks",
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp, vertical = 6.dp
                                            ).align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            } else {
                                items(state.suggestedTasks,
                                    key = { task -> "suggested/task/${task.id}" },
                                    contentType = { "suggested/task" }) { task ->

                                    //val category = state.categories.firstOrNull { it.id == task.categoryId }

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
                                            component.showTaskDetails(
                                                state.workspace!!.id,
                                                task.id
                                            )
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
                            for ((category, entry) in state.otherTasks) {
                                val tasks = entry.tasks
                                val incomplete = entry.incomplete

                                item(
                                    key = "category/header/${category?.id.toString()}",
                                    contentType = "category/header"
                                ) {
                                    ColorTheme(
                                        seed = category?.color?.blendWithParent()
                                            ?: LocalSeedColor.current
                                    ) {
                                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
                                            HorizontalDivider()
                                            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                shape = MaterialTheme.shapes.extraLarge,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                                    .padding(top = 12.dp),
                                                onClick = {
                                                    component.showCategoryDetails(
                                                        state.workspace!!.id, category?.id
                                                    )
                                                }) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(
                                                        horizontal = 12.dp, vertical = 6.dp
                                                    )
                                                ) {
                                                    Text(
                                                        modifier = Modifier.align(Alignment.CenterVertically),
                                                        text = category?.name ?: "Uncategorized",
                                                        style = MaterialTheme.typography.titleLarge,
                                                    )
                                                    Spacer(Modifier.weight(1f))
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = "View category",
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                items(tasks,
                                    key = { "category/task/${it.id}" },
                                    contentType = { "category/task" }) { task ->
                                    ColorTheme(
                                        seed = category?.color?.blendWithParent()
                                            ?: LocalSeedColor.current
                                    ) {
                                        assignmentDialog(task)

                                        Task(
                                            task,
                                            assignedTo = state.members.filter {
                                                task.assignedTo.contains(
                                                    it.userId
                                                )
                                            },
                                            selected = showActionMenuForTask == task.id,
                                            onClick = {
                                                component.showTaskDetails(
                                                    state.workspace!!.id, task.id
                                                )
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
                                            },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        ) {
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


                                if (incomplete > 0) {
                                    item(
                                        key = "category/more/${category?.id.toString()}",
                                        contentType = "category/more"
                                    ) {
                                        ColorTheme(
                                            seed = category?.color?.blendWithParent()
                                                ?: LocalSeedColor.current
                                        ) {
                                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
                                                Surface(color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                    shape = MaterialTheme.shapes.extraLarge,
                                                    modifier = Modifier.padding(horizontal = 12.dp)
                                                        .padding(bottom = 12.dp),
                                                    onClick = {
                                                        component.showCategoryDetails(
                                                            state.workspace!!.id, category?.id
                                                        )
                                                    }) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(
                                                            horizontal = 12.dp, vertical = 6.dp
                                                        )
                                                    ) {
                                                        Text(
                                                            modifier = Modifier.align(Alignment.CenterVertically),
                                                            text = "+$incomplete more",
                                                        )
                                                        Spacer(Modifier.weight(1f))
                                                        Icon(
                                                            Icons.AutoMirrored.Filled.OpenInNew,
                                                            contentDescription = "View category",
                                                            modifier = Modifier.align(Alignment.CenterVertically)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (tasks.isEmpty()) {
                                    item(
                                        key = "category/placeholder/${category?.id.toString()}",
                                        contentType = "category/placeholder"
                                    ) {
                                        ColorTheme(
                                            seed = category?.color?.blendWithParent()
                                                ?: LocalSeedColor.current
                                        ) {
                                            Row(
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                    .padding(12.dp).fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "All tasks complete!",
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp, vertical = 6.dp
                                                    ).align(Alignment.CenterVertically)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            item(key = "new") {
                                HorizontalDivider()
                                Surface(color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = MaterialTheme.shapes.extraLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(top = 12.dp)
                                        .padding(bottom = insets.calculateBottomPadding() + 80.dp),
                                    onClick = {
                                        state.workspace?.id?.let { workspaceId ->
                                            component.showCreateCategory(workspaceId)
                                        }
                                    }) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(
                                            horizontal = 12.dp, vertical = 6.dp
                                        )
                                    ) {
                                        Text(
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            text = "Add category",
                                            style = MaterialTheme.typography.titleLarge,
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = "New category",
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, backLayerContent = {
            var showActionMenuForWorkspace by remember { mutableStateOf<Id?>(null) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)
            ) {
                items(state.workspaces) { workspace ->
                    WorkspacePreview(workspace = workspace, onClick = {
                        if (state.workspace?.id == workspace.id && showActionMenuForWorkspace == workspace.id) {
                            coroutineScope.launch {
                                scaffoldState.conceal()
                            }
                        } else {
                            component.selectWorkspace(workspace.id)
                            showActionMenuForWorkspace = null
                        }
                    }, onLongClick = { selected ->
                        showActionMenuForWorkspace = if (selected) {
                            null
                        } else {
                            workspace.id
                        }
                    }, selected = showActionMenuForWorkspace == workspace.id
                    ) {
                        WorkspaceActions(onEdit = {
                            component.showUpdateWorkspace(workspace.id)
                        }, onCustomize = {
                            component.showCustomizeWorkspace(workspace.id)
                        }, onShare = {
                            component.showShareWorkspace(workspace.id)
                        })
                    }
                }

                item {
                    NewWorkspace(!state.isLoading) {
                        component.showCreateWorkspace()
                    }
                }
            }
        })
    }
}
package dev.chara.taskify.shared.ui.content.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.component.sheet.ShareSheetComponent
import dev.chara.taskify.shared.ui.component.ProfilePhoto
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.harmonizeWithParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheetContent(component: ShareSheetComponent) {
    val state by component.state.collectAsState()

    if (state.isLoading) {
        return
    }

    var showRemoveDialogFor by remember { mutableStateOf<String?>(null) }

    var showLeaveDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState { value ->
        value != SheetValue.Hidden || !state.leaveInProgress
    }

    ColorTheme(seed = state.workspace?.color?.harmonizeWithParent() ?: LocalSeedColor.current) {
        if (showRemoveDialogFor != null) {
            val user = state.members.firstOrNull { it.userId == showRemoveDialogFor }

            AlertDialog(onDismissRequest = {
                showRemoveDialogFor = null
            }, confirmButton = {
                TextButton(onClick = {
                    showRemoveDialogFor = null
                    component.removeMember(user!!.userId)
                }) {
                    Text("Remove")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    showRemoveDialogFor = null
                }) {
                    Text("Cancel")
                }
            }, title = {
                Text("Remove ${user?.email ?: "user"}?")
            })
        }

        if (showLeaveDialog) {
            AlertDialog(onDismissRequest = {
                showLeaveDialog = false
            }, confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    component.leaveWorkspace()
                }) {
                    Text("Leave")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                }) {
                    Text("Cancel")
                }
            }, title = {
                Text("Leave workspace?")
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
                    state.workspace?.name ?: "Sharing",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = {
                        Text(state.owner?.email ?: "")
                    },
                    leadingContent = {
                        ProfilePhoto(state.owner)
                    },
                    trailingContent = {
                        Text("Workspace Owner")
                    }
                )

                for (member in state.members) {
                    ListItem(
                        headlineContent = {
                            Text(member.email)
                        },
                        leadingContent = {
                           ProfilePhoto(member)
                        },
                        trailingContent = {
                            if (state.owner?.userId == state.current?.userId) {
                                IconButton(onClick = {
                                    showRemoveDialogFor = member.userId
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = null)
                                }
                            }
                        }
                    )
                }

                ListItem(
                    modifier = Modifier.clickable {
                        if (!state.leaveInProgress) {
                            component.shareWorkspace()
                        }
                    },
                    headlineContent = {
                        Text("Invite members")
                    },
                    leadingContent = {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null)
                    },
                )

                Row {
                    if (state.owner?.userId != state.current?.userId) {
                        OutlinedButton(modifier = Modifier.padding(
                            horizontal = 16.dp, vertical = 8.dp
                        ), onClick = {
                            showLeaveDialog = true
                        }, enabled = !state.leaveInProgress
                        ) {
                            Text("Leave", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    FilledTonalButton(modifier = Modifier.padding(
                        horizontal = 16.dp, vertical = 8.dp
                    ), onClick = {
                        component.cancel()
                    }, enabled = !state.leaveInProgress
                    ) {
                        Text("OK", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
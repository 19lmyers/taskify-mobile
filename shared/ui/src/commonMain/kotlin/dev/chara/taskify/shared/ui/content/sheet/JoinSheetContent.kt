package dev.chara.taskify.shared.ui.content.sheet

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.component.sheet.JoinSheetComponent
import dev.chara.taskify.shared.ui.component.ProfilePhoto
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalSeedColor
import dev.chara.taskify.shared.ui.theme.harmonizeWithParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinSheetContent(component: JoinSheetComponent) {
    val state by component.state.collectAsState()

    if (state.isLoading) {
        return
    }

    val sheetState = rememberModalBottomSheetState { value ->
        value != SheetValue.Hidden || !state.joinInProgress
    }

    ColorTheme(seed = state.workspace?.color?.harmonizeWithParent() ?: LocalSeedColor.current) {
        if (state.inviteIsValid) {
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
                        state.workspace?.name ?: "Join",
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
                            }
                        )
                    }

                    Row {
                        TextButton(
                            modifier = Modifier.padding(
                                horizontal = 16.dp, vertical = 8.dp
                            ), onClick = {
                                component.cancel()
                            }, enabled = !state.joinInProgress
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(Modifier.weight(1f))

                        FilledTonalButton(
                            modifier = Modifier.padding(
                                horizontal = 16.dp, vertical = 8.dp
                            ), onClick = {
                                component.join()
                            }, enabled = !state.joinInProgress
                        ) {
                            Text("Join", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        } else {
            AlertDialog(onDismissRequest = {
                component.cancel()
            }, confirmButton = {
                TextButton(onClick = {
                    component.cancel()
                }) {
                    Text("OK")
                }
            }, title = {
                Text("Invalid invite")
            }, text = {
                Text("This invite has expired")
            })
        }
    }
}
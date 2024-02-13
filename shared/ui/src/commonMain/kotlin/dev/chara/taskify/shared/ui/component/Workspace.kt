package dev.chara.taskify.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.model.Workspace
import dev.chara.taskify.shared.ui.theme.ColorTheme
import dev.chara.taskify.shared.ui.theme.LocalBaseColor
import dev.chara.taskify.shared.ui.theme.harmonizeWithParent


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkspacePreview(
    workspace: Workspace,
    onClick: (() -> Unit)? = null,
    onLongClick: ((Boolean) -> Unit)? = null,
    selected: Boolean = false,
    actions: (@Composable () -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current

    ColorTheme(seed = workspace.color?.harmonizeWithParent() ?: LocalBaseColor.current) {
        Surface(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .combinedClickable(enabled = onClick != null || onLongClick != null, onClick = {
                    onClick?.invoke()
                }, onLongClick = {
                    onLongClick?.let {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        it(selected)
                    }
                }),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Column {
                Row(Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Filled.Workspaces,
                        contentDescription = null,
                        modifier = Modifier.align(
                            Alignment.CenterVertically
                        )
                    )
                    Column(modifier = Modifier.align(Alignment.CenterVertically).padding(start = 16.dp)) {
                        Text(
                            text = workspace.name,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
                actions?.let {
                    AnimatedVisibility(selected) {
                        HorizontalDivider()
                        it()
                    }
                }
            }
        }
    }
}

@Composable
fun NewWorkspace(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent,
        border = BorderStroke(
            1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Icon(
                    Icons.Filled.Add, contentDescription = null, modifier = Modifier.align(
                        Alignment.CenterVertically
                    )
                )
                Text(
                    text = "New workspace",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 16.dp)
                )
            }
        }
    }
}

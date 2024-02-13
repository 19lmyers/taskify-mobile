package dev.chara.taskify.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.domain.isOverdue
import dev.chara.taskify.shared.model.MutableTask
import dev.chara.taskify.shared.model.Profile
import dev.chara.taskify.shared.model.Task


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Task(
    task: Task,
    onUpdate: ((MutableTask) -> Unit) -> Unit,
    assignedTo: List<Profile>,
    description: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: ((Boolean) -> Unit)? = null,
    selected: Boolean = false,
    actions: (@Composable () -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current

    val surfaceColor by animateColorAsState(
        if (selected) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            Color.Transparent
        }
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .combinedClickable(
                    enabled = onClick != null || onLongClick != null,
                    onClick = {
                        onClick?.invoke()
                    },
                    onLongClick = {
                        onLongClick?.let {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            it(selected)
                        }
                    }
                ),
            color = surfaceColor,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column {
                TaskContents(
                    task,
                    assignedTo = assignedTo,
                    description = description,
                    onUpdate = onUpdate
                )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskContents(
    task: Task,
    description: String?,
    assignedTo: List<Profile>,
    onUpdate: ((MutableTask) -> Unit) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp)
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            modifier = Modifier.align(Alignment.Top),
            checked = task.status == Task.Status.Complete,
            onCheckedChange = { checked ->
                onUpdate {
                    it.status = if (checked) {
                        Task.Status.Complete
                    } else {
                        Task.Status.NotStarted
                    }
                    it.scheduledAt = null
                }
            },
            colors = CheckboxDefaults.colors(
                uncheckedColor = if (task.scheduledAt?.isOverdue() == true) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        )

        Column(
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp),
        ) {
            Text(
                text = task.name,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                style = if (task.status == Task.Status.InProgress || task.scheduledAt?.isOverdue() == true) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = if (task.status == Task.Status.InProgress) {
                    MaterialTheme.colorScheme.primary
                } else if (task.scheduledAt?.isOverdue() == true) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color.Unspecified
                }
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (task.status == Task.Status.InProgress) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    } else if (task.scheduledAt?.isOverdue() == true) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (assignedTo.isNotEmpty()) {
            FlowRow(modifier = Modifier.align(Alignment.CenterVertically)) {
                for (profile in assignedTo) {
                    ProfilePhoto(profile)
                }
            }
        }
    }
}
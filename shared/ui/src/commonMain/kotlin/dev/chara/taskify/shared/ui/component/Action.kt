package dev.chara.taskify.shared.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.model.Task

@Composable
fun RowScope.Action(
    icon: @Composable () -> Unit, label: @Composable () -> Unit, onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.weight(1f),
        color = Color.Transparent,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)) {
                icon()
            }
            Box(modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)) {
                label()
            }
        }
    }
}


@Composable
fun WorkspaceActions(
    onEdit: () -> Unit, onCustomize: () -> Unit, onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Action(icon = {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }, label = {
            Text("Edit")
        }, onClick = onEdit
        )
        Action(icon = {
            Icon(Icons.Filled.Tune, contentDescription = null)
        }, label = {
            Text("Customize")
        }, onClick = onCustomize)
        Action(icon = {
            Icon(Icons.Filled.Share, contentDescription = null)
        }, label = {
            Text("Share")
        }, onClick = onShare
        )
    }
}


@Composable
fun TaskActions(
    task: Task,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onSchedule: () -> Unit,
    onAssign: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (task.status == Task.Status.NotStarted) {
            Action(icon = {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
            }, label = {
                Text("Do now")
            }, onClick = onStart
            )
        }

        if (task.status == Task.Status.InProgress || task.scheduledAt != null) {
            Action(icon = {
                Icon(Icons.Filled.Event, contentDescription = null)
            }, label = {
                Text("Do later")
            }, onClick = onCancel
            )
        }

        if (task.scheduledAt == null) {
            Action(icon = {
                Icon(Icons.Filled.Schedule, contentDescription = null)
            }, label = {
                Text("Schedule")
            }, onClick = onSchedule
            )
        }

        Action(icon = {
            Icon(Icons.Filled.PersonAdd, contentDescription = null)
        }, label = {
            Text("Assign")
        }, onClick = onAssign
        )
    }
}
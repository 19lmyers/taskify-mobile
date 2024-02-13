package dev.chara.taskify.shared.ui.content.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.domain.util.formatDefault
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(onDismiss: () -> Unit, onConfirm: (LocalDateTime) -> Unit) {
    val localDateTime =
        Clock.System.now().plus(1.hours).toLocalDateTime(TimeZone.currentSystemDefault())

    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = localDateTime.date.atStartOfDayIn(
            TimeZone.UTC
        ).toEpochMilliseconds(), selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= localDateTime.date.atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds()
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= localDateTime.date.year
            }
        })

    var selectedTime by remember { mutableStateOf(localDateTime.hour to localDateTime.minute) }

    var showTimePickerDialog by remember { mutableStateOf(false) }

    if (showTimePickerDialog) {
        TimePickerDialog(onDismiss = { showTimePickerDialog = false }, onConfirm = { hour, minute ->
            selectedTime = hour to minute
            showTimePickerDialog = false
        }, initialHour = selectedTime.first, initialMinute = selectedTime.second
        )
    }

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = {
                onConfirm(
                    Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis!!)
                        .plus(selectedTime.first.hours).plus(selectedTime.second.minutes)
                        .toLocalDateTime(TimeZone.UTC)
                )
            }, enabled = datePickerState.selectedDateMillis != null
        ) {
            Text("OK")
        }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            DatePicker(
                state = datePickerState, colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }

        HorizontalDivider()

        ListItem(colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
            modifier = Modifier.clickable { showTimePickerDialog = true }
                .padding(horizontal = 8.dp),
            headlineContent = {
                Text(
                    LocalTime(selectedTime.first, selectedTime.second).formatDefault()
                )
            },
            leadingContent = { Icon(Icons.Filled.Schedule, contentDescription = "Clock") })

        HorizontalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    dismissButton: (@Composable () -> Unit)?,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.wrapContentHeight(),
    ) {
        Surface(
            modifier = Modifier.requiredWidth(360.dp),
            shape = DatePickerDefaults.shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = DatePickerDefaults.TonalElevation,
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                content()

                Box(
                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp, end = 6.dp),
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.primary
                    ) {
                        val textStyle = MaterialTheme.typography.labelLarge
                        ProvideTextStyle(value = textStyle) {
                            FlowRow {
                                dismissButton?.invoke()
                                confirmButton()
                            }
                        }
                    }
                }
            }
        }
    }
}

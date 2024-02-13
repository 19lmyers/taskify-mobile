package dev.chara.taskify.android.notification.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.chara.taskify.android.R
import dev.chara.taskify.android.model.res
import dev.chara.taskify.android.notification.Action
import dev.chara.taskify.android.notification.receiver.NotificationActionReceiver
import dev.chara.taskify.android.ui.MainActivity
import dev.chara.taskify.shared.domain.use_case.account.LinkFcmTokenUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.GetCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.GetTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.GetWorkspaceUseCase
import dev.chara.taskify.shared.model.Id
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

internal class ReceivedId(override val hexString: String) : Id()

class MessagingService : FirebaseMessagingService(), LifecycleOwner {

    private val dispatcher = ServiceLifecycleDispatcher(this)

    override val lifecycle: Lifecycle = dispatcher.lifecycle

    private val linkFcmTokenUseCase: LinkFcmTokenUseCase by inject()

    private val getTaskUseCase: GetTaskUseCase by inject()
    private val getCategoryUseCase: GetCategoryUseCase by inject()
    private val getWorkspaceUseCase: GetWorkspaceUseCase by inject()

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onStart(intent: Intent, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun onNewToken(token: String) {
        lifecycleScope.launch {
            linkFcmTokenUseCase(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val messageType = message.data[DATA_MESSAGE_TYPE]

        if (messageType == MESSAGE_TYPE_ACTION) {
            val actor = message.data[DATA_ACTOR] ?: return

            val action = message.data[DATA_ACTION]?.let { actionString ->
                Action.entries.firstOrNull { it.name == actionString }
            } ?: return

            val taskId = message.data[DATA_TASK_ID]?.let {
                ReceivedId(it)
            } ?: return

            lifecycleScope.launch {
                val task = getTaskUseCase(taskId).first() ?: return@launch

                val category = task.categoryId?.let {
                    getCategoryUseCase(it).first()
                }

                val workspace = task.workspaceId?.let {
                    getWorkspaceUseCase(it).first()
                } ?: return@launch

                val body = when (action) {
                    Action.AddTask -> "$actor added"
                    Action.RemoveTask -> "$actor removed"
                    Action.CompleteTask -> "$actor completed"
                }

                val groupId = workspace.id.hexString
                val groupName = workspace.name

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannelGroup(
                    NotificationChannelGroup(
                        groupId, groupName
                    )
                )

                val actionChannelId = "${workspace.id.hexString}/actions"

                val viewWorkspaceIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.Builder().scheme("https").authority(MainActivity.DEEP_LINK_HOST)
                        //.path(MainActivity.PATH_VIEW_LIST)
                        //.appendQueryParameter(MainActivity.QUERY_LIST_ID, listId)
                        .build()
                )

                val viewWorkspacePendingIntent = PendingIntent.getActivity(
                    this@MessagingService,
                    0,
                    viewWorkspaceIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                )

                var builder = NotificationCompat.Builder(this@MessagingService, actionChannelId)
                    .setContentTitle(task.name)
                    .setContentText(body)
                    .setSubText(category?.name ?: "Uncategorized")
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.baseline_fact_check_24)
                    .setWhen(message.sentTime)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(viewWorkspacePendingIntent)
                    .setAutoCancel(true)
                    .setGroup(GROUP_ACTIONS)

                if (category?.color != null) {
                    builder = builder.setColor(resources.getColor(category.color!!.res, theme))
                } else if (workspace.color != null) {
                    builder = builder.setColor(resources.getColor(workspace.color!!.res, theme))
                }

                if (ActivityCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val channel = NotificationChannel(
                        actionChannelId, "Activity", NotificationManager.IMPORTANCE_LOW
                    )
                    channel.group = groupId

                    notificationManager.createNotificationChannel(channel)

                    NotificationManagerCompat.from(this@MessagingService)
                        .notify(taskId.hexString, NOTIFICATION_TYPE_ACTION, builder.build())

                    var summaryBuilder =
                        NotificationCompat.Builder(this@MessagingService, actionChannelId)
                            .setSmallIcon(R.drawable.baseline_fact_check_24)
                            .setContentTitle("Shared list activity").setSubText(workspace.name)
                            .setGroup(GROUP_ACTIONS)
                            .setGroupSummary(true)

                    if (workspace.color != null) {
                        summaryBuilder = summaryBuilder.setColor(
                            resources.getColor(
                                workspace.color!!.res,
                                theme
                            )
                        )
                    }

                    NotificationManagerCompat.from(this@MessagingService)
                        .notify(
                            workspace.id.hexString,
                            NOTIFICATION_TYPE_ACTION,
                            summaryBuilder.build()
                        )
                }
            }
        } else if (messageType == MESSAGE_TYPE_REMINDER) {
            val taskId = message.data[DATA_TASK_ID]?.let {
                ReceivedId(it)
            } ?: return

            lifecycleScope.launch {
                val task = getTaskUseCase(taskId).first() ?: return@launch

                val category = task.categoryId?.let {
                    getCategoryUseCase(it).first()
                }

                val workspace = task.workspaceId?.let {
                    getWorkspaceUseCase(it).first()
                } ?: return@launch

                val groupId = workspace.id.hexString
                val groupName = workspace.name

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannelGroup(
                    NotificationChannelGroup(
                        groupId, groupName
                    )
                )

                val reminderChannelId = "${workspace.id.hexString}/reminders"

                val editTaskIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.Builder()
                            .scheme("https")
                            .authority(MainActivity.DEEP_LINK_HOST)
                            //.path(MainActivity.PATH_VIEW_TASK)
                            //.appendQueryParameter(MainActivity.QUERY_TASK_ID, taskId)
                            .build()
                    )

                val editTaskPendingIntent =
                    PendingIntent.getActivity(
                        this@MessagingService,
                        0,
                        editTaskIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    )

                val completeTaskIntent =
                    Intent(this@MessagingService, NotificationActionReceiver::class.java)
                        .setAction(NotificationActionReceiver.ACTION_COMPLETE_TASK)
                        .putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId.hexString)

                val completeTaskPendingIntent =
                    PendingIntent.getBroadcast(
                        this@MessagingService,
                        0,
                        completeTaskIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    )

                var builder = NotificationCompat.Builder(this@MessagingService, reminderChannelId)
                    .setContentTitle(task.name)
                    .setSubText(category?.name ?: "Uncategorized")
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.baseline_fact_check_24)
                    .setWhen(message.sentTime)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(editTaskPendingIntent)
                    .addAction(R.drawable.baseline_done_24, "Mark as complete", completeTaskPendingIntent)
                    .setAutoCancel(true)
                    .setGroup(GROUP_REMINDERS)

                if (category?.color != null) {
                    builder = builder.setColor(resources.getColor(category.color!!.res, theme))
                } else if (workspace.color != null) {
                    builder = builder.setColor(resources.getColor(workspace.color!!.res, theme))
                }

                if (ActivityCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val channel = NotificationChannel(
                        reminderChannelId, "Reminders", NotificationManager.IMPORTANCE_HIGH
                    )
                    channel.group = groupId

                    notificationManager.createNotificationChannel(channel)

                    NotificationManagerCompat.from(this@MessagingService)
                        .notify(taskId.hexString, NOTIFICATION_TYPE_REMINDER, builder.build())

                    var summaryBuilder =
                        NotificationCompat.Builder(this@MessagingService, reminderChannelId)
                            .setSmallIcon(R.drawable.baseline_fact_check_24)
                            .setContentTitle("Reminders").setSubText(workspace.name)
                            .setGroup(GROUP_REMINDERS)
                            .setGroupSummary(true)

                    if (workspace.color != null) {
                        summaryBuilder = summaryBuilder.setColor(
                            resources.getColor(
                                workspace.color!!.res,
                                theme
                            )
                        )
                    }

                    NotificationManagerCompat.from(this@MessagingService)
                        .notify(
                            workspace.id.hexString,
                            NOTIFICATION_TYPE_REMINDER,
                            summaryBuilder.build()
                        )
                }
            }
        } else if (messageType == MESSAGE_TYPE_ASSIGNED) {
            val actor = message.data[DATA_ACTOR] ?: return

            val taskId = message.data[DATA_TASK_ID]?.let {
                ReceivedId(it)
            } ?: return

            lifecycleScope.launch {
                val task = getTaskUseCase(taskId).first() ?: return@launch

                val category = task.categoryId?.let {
                    getCategoryUseCase(it).first()
                }

                val workspace = task.workspaceId?.let {
                    getWorkspaceUseCase(it).first()
                } ?: return@launch

                val body = "$actor assigned to you"

                val groupId = workspace.id.hexString
                val groupName = workspace.name

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannelGroup(
                    NotificationChannelGroup(
                        groupId, groupName
                    )
                )

                val actionChannelId = "${workspace.id.hexString}/assigned"

                val viewWorkspaceIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.Builder().scheme("https").authority(MainActivity.DEEP_LINK_HOST)
                        //.path(MainActivity.PATH_VIEW_LIST)
                        //.appendQueryParameter(MainActivity.QUERY_LIST_ID, listId)
                        .build()
                )

                val viewWorkspacePendingIntent = PendingIntent.getActivity(
                    this@MessagingService,
                    0,
                    viewWorkspaceIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                )

                var builder = NotificationCompat.Builder(this@MessagingService, actionChannelId)
                    .setContentTitle(task.name)
                    .setContentText(body)
                    .setSubText(category?.name ?: "Uncategorized")
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.baseline_fact_check_24)
                    .setWhen(message.sentTime)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(viewWorkspacePendingIntent)
                    .setAutoCancel(true)
                    .setGroup(GROUP_ASSIGNED)

                if (category?.color != null) {
                    builder = builder.setColor(resources.getColor(category.color!!.res, theme))
                } else if (workspace.color != null) {
                    builder = builder.setColor(resources.getColor(workspace.color!!.res, theme))
                }

                if (ActivityCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val channel = NotificationChannel(
                        actionChannelId, "Assigned to you", NotificationManager.IMPORTANCE_HIGH
                    )
                    channel.group = groupId

                    notificationManager.createNotificationChannel(channel)

                    NotificationManagerCompat.from(this@MessagingService)
                        .notify(taskId.hexString, NOTIFICATION_TYPE_ASSIGNED, builder.build())

                    var summaryBuilder =
                        NotificationCompat.Builder(this@MessagingService, actionChannelId)
                            .setSmallIcon(R.drawable.baseline_fact_check_24)
                            .setContentTitle("Assigned to you").setSubText(workspace.name)
                            .setGroup(GROUP_ASSIGNED)
                            .setGroupSummary(true)

                    if (workspace.color != null) {
                        summaryBuilder = summaryBuilder.setColor(
                            resources.getColor(
                                workspace.color!!.res,
                                theme
                            )
                        )
                    }

                    NotificationManagerCompat.from(this@MessagingService)
                        .notify(
                            workspace.id.hexString,
                            NOTIFICATION_TYPE_ASSIGNED,
                            summaryBuilder.build()
                        )
                }
            }
        }
    }

    companion object {
        private const val DATA_MESSAGE_TYPE = "DATA_MESSAGE_TYPE"

        private const val MESSAGE_TYPE_ACTION = "MESSAGE_TYPE_ACTION"
        private const val MESSAGE_TYPE_REMINDER = "MESSAGE_TYPE_REMINDER"
        private const val MESSAGE_TYPE_ASSIGNED = "MESSAGE_TYPE_ASSIGNED"

        private const val DATA_TASK_ID = "DATA_TASK_ID"
        private const val DATA_ACTOR = "DATA_ACTOR"
        private const val DATA_ACTION = "DATA_ACTION"

        const val NOTIFICATION_TYPE_ACTION = 1
        const val NOTIFICATION_TYPE_REMINDER = 2
        const val NOTIFICATION_TYPE_ASSIGNED = 3

        const val GROUP_ACTIONS = "ACTIONS"
        const val GROUP_REMINDERS = "REMINDERS"
        const val GROUP_ASSIGNED = "ASSIGNED"
    }
}

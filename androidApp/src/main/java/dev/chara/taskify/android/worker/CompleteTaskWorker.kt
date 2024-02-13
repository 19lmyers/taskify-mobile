package dev.chara.taskify.android.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.chara.taskify.android.notification.service.MessagingService
import dev.chara.taskify.shared.domain.use_case.data.task.UpdateTaskUseCase
import dev.chara.taskify.shared.model.Id
import dev.chara.taskify.shared.model.Task
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class ReceivedId(override val hexString: String) : Id()

class CompleteTaskWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), KoinComponent {

    private val updateTaskUseCase: UpdateTaskUseCase by inject()

    override suspend fun doWork(): Result {
        val idString = inputData.getString(TASK_ID) ?: return Result.failure()
        val taskId: Id = ReceivedId(idString)

        updateTaskUseCase(taskId) {
            it.status = Task.Status.Complete
        }

        NotificationManagerCompat.from(applicationContext)
            .cancel(idString, MessagingService.NOTIFICATION_TYPE_REMINDER)

        return Result.success()
    }

    companion object {
        const val TASK_ID = "TASK_ID"
    }
}

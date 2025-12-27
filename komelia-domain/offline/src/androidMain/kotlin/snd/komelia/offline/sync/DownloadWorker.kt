package snd.komelia.offline.sync

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Semaphore
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadCompleted
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadError
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadProgress
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logError
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logInfo
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komga.client.book.KomgaBookId
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

internal const val bookIdDataKey = "bookId"
private val notificationIdCounter = AtomicInteger(1)
const val downloadChannelId = "downloads_channel"
private val jobLimit = Semaphore(4)

private val logger = KotlinLogging.logger { }

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val downloadService: BookDownloadService,
    private val logsJournalRepository: LogJournalRepository,
    private val sharedEvents: MutableSharedFlow<DownloadEvent>,
) : CoroutineWorker(context, workerParams) {
    private val notificationId = notificationIdCounter.incrementAndGet()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = createBaseNotification().build()

        return ForegroundInfo(
            notificationId,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
    }

    override suspend fun doWork(): Result {
        val bookId = inputData.getString(bookIdDataKey)
        val result = when {
            bookId != null -> downloadBook(KomgaBookId(bookId))
            else -> Result.failure()
        }

        return result
    }


    private suspend fun downloadBook(bookId: KomgaBookId): Result {
        val isSuccess = AtomicBoolean(false)

        try {
            downloadService.downloadBook(bookId)
                .onEach { sharedEvents.emit(it) }
                .conflate()
                .collect {
                    when (it) {
                        is BookDownloadProgress -> {
                            updateProgress(it)
                            delay(200)
                        }

                        is BookDownloadCompleted -> {
                            logsJournalRepository.logInfo { "Book downloaded ${it.book.metadata.title}" }
                            isSuccess.set(true)
                        }

                        is BookDownloadError -> {
                            setErrorNotification(it)
                            logsJournalRepository.logError(it.error) { "Book downloaded error ${it.book?.metadata?.title ?: it.bookId}" }
                            isSuccess.set(false)
                        }
                    }
                }
        } catch (e: Throwable) {
            logger.catching(e)
            sharedEvents.emit(BookDownloadError(bookId = bookId, error = e))
            currentCoroutineContext().ensureActive()
            return Result.failure()
        } finally {
            if (isSuccess.get()) {
                applicationContext.cancelNotification(notificationId)
            }
        }

        return if (isSuccess.get()) Result.success() else Result.failure()
    }

    private fun setErrorNotification(event: BookDownloadError) {
        val notification = createBaseNotification()
        notification.setContentTitle(event.book?.metadata?.title ?: event.bookId.value)
        val errorMessage =
            if (event.error is CancellationException) "Cancelled"
            else event.error.message ?: event.error::class.simpleName
        notification.setContentText(errorMessage)
        applicationContext.notify(notificationId, notification.build())
    }

    private fun updateProgress(progress: BookDownloadProgress) {
        val notification = createBaseNotification()

        // can't use long for progress
        // normalize to int range 0..1000
        val total = if (progress.total == 0L) 0 else 1000
        val completed =
            if (progress.total == 0L || progress.completed == 0L) 0
            else ((progress.completed.toFloat() / progress.total) * 1000).roundToInt()

        notification.setContentTitle(progress.book.metadata.title)
        notification.setProgress(total, completed, total == 0)

        applicationContext.notify(notificationId, notification.build())
    }

    fun Context.notify(id: Int, notification: Notification) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(id, notification)
    }

    fun Context.cancelNotification(id: Int) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).cancel(id)
    }

    private fun createBaseNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, downloadChannelId)
            .setContentTitle("Downloading")
            .setSmallIcon(android.R.drawable.stat_sys_download)
    }
}
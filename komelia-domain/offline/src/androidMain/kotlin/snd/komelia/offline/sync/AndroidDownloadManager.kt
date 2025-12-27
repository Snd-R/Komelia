package snd.komelia.offline.sync

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import snd.komga.client.book.KomgaBookId

/**
 *  recommended way to manage long-running download tasks
 *  should be able to execute and reschedule the work even if app is killed in background
 *  https://developer.android.com/reference/androidx/work/WorkManager
 */
class AndroidDownloadManager(
    private val context: Context,
) : PlatformDownloadManager {

    override suspend fun launchBookDownload(bookId: KomgaBookId) {

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                Data.Builder()
                    .putString(bookIdDataKey, bookId.value)
                    .build()
            ).build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(bookId.value, ExistingWorkPolicy.REPLACE, request)
    }

    override suspend fun cancelBookDownload(bookId: KomgaBookId) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(bookId.value)
    }
}
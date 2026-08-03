package snd.komelia

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import snd.komelia.offline.OfflineDependencies
import snd.komelia.offline.sync.DownloadWorker

class MyWorkerFactory(
    private val dependencies: Flow<OfflineDependencies>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return runBlocking {
            val currentDependencies = dependencies.first()
            DownloadWorker(
                context = appContext,
                workerParams = workerParameters,
                downloadService = currentDependencies.downloadService,
                logsJournalRepository = currentDependencies.repositories.logJournalRepository,
                sharedEvents = currentDependencies.bookDownloadEvents,
            )
        }
    }
}
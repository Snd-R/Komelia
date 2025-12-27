package snd.komelia

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.work.Configuration
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import snd.komelia.offline.sync.downloadChannelId
import snd.komelia.ui.DependencyContainer

val dependencies = MutableStateFlow<DependencyContainer?>(null)
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalExceptionHandler.initialize(applicationContext)
        setupNotificationChannels()
        initWorkManager()
    }

    private fun setupNotificationChannels() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat
                    .Builder(downloadChannelId, IMPORTANCE_LOW)
                    .setName("downloads")
                    .setShowBadge(false)
                    .build()
            )
        )
    }

    private fun initWorkManager() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(MyWorkerFactory(dependencies.filterNotNull().map { it.offlineDependencies }))
            .setWorkerCoroutineContext(Dispatchers.IO.limitedParallelism(4))
            .build()
        WorkManager.initialize(this, config)
    }
}
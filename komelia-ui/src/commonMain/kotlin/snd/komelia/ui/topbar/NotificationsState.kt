package snd.komelia.ui.topbar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komga.client.book.KomgaBook
import snd.komga.client.sse.KomgaEvent
import kotlin.time.Clock

private typealias NotificationId = String

class NotificationsState(
    private val komgaEvents: SharedFlow<KomgaEvent>,
    bookDownloadEvents: SharedFlow<DownloadEvent>,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val notificationMap: MutableStateFlow<Map<NotificationId, Notification>> = MutableStateFlow(emptyMap())

    val unreadNotifications = MutableStateFlow(0)
    val notificationsOpen = MutableStateFlow(false)
    val notifications: SharedFlow<Collection<Notification>> = notificationMap
        .map { it.values.reversed() }
        .shareIn(coroutineScope, SharingStarted.Eagerly, 1)


    init {
        bookDownloadEvents.onEach { event ->
            when (event) {
                is DownloadEvent.BookDownloadProgress -> handleBookProgress(event)
                is DownloadEvent.BookDownloadCompleted -> handleBookCompleted(event)
                is DownloadEvent.BookDownloadError -> {}
            }
        }.launchIn(coroutineScope)
    }

    private fun handleBookProgress(event: DownloadEvent.BookDownloadProgress) {
        val bookId = event.book.id.value
        val existing = notificationMap.value[bookId]
        if (existing == null && !notificationsOpen.value) unreadNotifications.update { it + 1 }

        notificationMap.update { notifications ->
            val mutable = notifications.toMutableMap()
            mutable[bookId] = BookNotification.BookDownloadProgress(
                book = event.book,
                timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                completedProgress = event.completed,
                totalProgress = event.total
            )
            mutable
        }
    }

    private fun handleBookCompleted(event: DownloadEvent.BookDownloadCompleted) {
        val bookId = event.book.id.value
        val existing = notificationMap.value[bookId]
        if (existing == null && !notificationsOpen.value) unreadNotifications.update { it + 1 }

        notificationMap.update { notifications ->
            val mutable = notifications.toMutableMap()
            mutable[bookId] = BookNotification.BookDownloadCompleted(
                book = event.book,
                timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
            mutable
        }
    }

    fun onNotificationsOpen() {
        notificationsOpen.value = true
        unreadNotifications.value = 0
    }

    fun onNotificationsClose() {
        notificationsOpen.value = false
        unreadNotifications.value = 0
    }

    fun onNotificationsClear() {
        notificationsOpen.value = false
        notificationMap.value = emptyMap()
    }

    fun onDispose() {
        coroutineScope.cancel()
    }

}

sealed interface Notification

sealed interface BookNotification : Notification {
    val book: KomgaBook
    val timestamp: LocalDateTime

    data class BookDownloadProgress(
        override val book: KomgaBook,
        override val timestamp: LocalDateTime,

        val completedProgress: Long,
        val totalProgress: Long,
    ) : BookNotification

    data class BookDownloadCompleted(
        override val book: KomgaBook,
        override val timestamp: LocalDateTime,
    ) : BookNotification

}

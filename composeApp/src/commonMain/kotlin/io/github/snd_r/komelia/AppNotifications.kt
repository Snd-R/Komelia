package io.github.snd_r.komelia

import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotification.Error
import io.github.snd_r.komelia.AppNotification.Normal
import io.github.snd_r.komelia.AppNotification.Success
import io.github.snd_r.komga.common.toErrorResponse
import io.github.snd_r.komga.common.toViolationResponse
import io.ktor.client.plugins.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class AppNotifications {
    private val notifications: MutableStateFlow<Map<UUID, AppNotification>> = MutableStateFlow(emptyMap())

    fun getNotifications(): Flow<Collection<AppNotification>> {
        return notifications.map { it.values }
    }

    fun remove(id: UUID) {
        notifications.update { current ->
            current.minus(id)
        }
    }

    fun add(notification: AppNotification) {
        notifications.update { current ->
            current.plus(notification.id to notification)
        }
    }

    fun runCatchingToNotifications(
        coroutineScope: CoroutineScope,
        block: suspend () -> Unit
    ) {
        coroutineScope.launch { runCatchingToNotifications { block() } }
    }

    suspend inline fun <R> runCatchingToNotifications(block: () -> R): Result<R> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            KotlinLogging.logger {}.warn(e) {}
            throw e
        } catch (e: Exception) {
            KotlinLogging.logger {}.error(e) {}
            toErrorNotification(e)
            return Result.failure(e)
        }
    }

    suspend inline fun toErrorNotification(exception: Exception) {
        when (exception) {
            is ClientRequestException -> {
                val errorMessage = exception.toErrorResponse()?.message
                    ?: exception.toViolationResponse()?.violations?.firstOrNull()
                        ?.let { "${it.fieldName}: ${it.message}" }
                    ?: exception.message

                add(Error(errorMessage))

            }

            else -> {
                add(Error(exception.message ?: exception.cause?.message ?: "Unknown error"))
            }
        }

    }
}

sealed class AppNotification(val id: UUID) {
    class Success(val message: String) : AppNotification(UUID.randomUUID())
    class Normal(val message: String) : AppNotification(UUID.randomUUID())
    class Error(val message: String) : AppNotification(UUID.randomUUID())
}


fun AppNotification.toToast(): Toast {
    return when (this) {
        is Error -> Toast(id = id, message = message, type = ToastType.Error)
        is Success -> Toast(id = id, message = message, type = ToastType.Success)
        is Normal -> Toast(id = id, message = message, type = ToastType.Normal)
    }
}
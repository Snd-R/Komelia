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
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds

class AppNotifications {
    private val notifications: MutableStateFlow<Map<Long, AppNotification>> = MutableStateFlow(emptyMap())

    fun getNotifications(): Flow<Collection<AppNotification>> {
        return notifications.map { it.values }
    }

    fun remove(id: Long) {
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
            KotlinLogging.logger {}.catching(e)
            toErrorNotification(e)
            return Result.failure(e)
        }
    }

    suspend fun toErrorNotification(exception: Exception) {
        when (exception) {
            is ClientRequestException -> {
                val contentType = exception.response.headers[HttpHeaders.ContentType]
                val errorMessage = when (contentType) {
                    "application/json" -> parseJsonErrorMessage(exception)
                    else -> errorMessageFromStatusCode(exception.response.status)
                }

                add(Error(errorMessage))
            }

            else -> {
                add(Error(exception.message ?: exception.cause?.message ?: "Unknown error"))
            }
        }

    }
}

private suspend fun parseJsonErrorMessage(exception: ClientRequestException): String {
    return exception.toErrorResponse()?.message
        ?: exception.toViolationResponse()
            ?.violations?.firstOrNull()?.let { "${it.fieldName}: ${it.message}" }
        ?: exception.response.bodyAsText()
}

private fun errorMessageFromStatusCode(statusCode: HttpStatusCode): String {
    return when (statusCode.value) {
        400 -> "Bad Request"
        404 -> "Not Found"
        413 -> "Content Too Large"
        418 -> "I'm a teapot"
        else -> HttpStatusCode.fromValue(statusCode.value).description
    }
}

sealed class AppNotification(val id: Long = Clock.System.now().epochSeconds) {
    class Success(val message: String) : AppNotification()
    class Normal(val message: String) : AppNotification()
    class Error(val message: String) : AppNotification()
}


fun AppNotification.toToast(): Toast {
    return when (this) {
        is Error -> Toast(id = id, message = message, type = ToastType.Error)
        is Success -> Toast(id = id, message = message, type = ToastType.Success)
        is Normal -> Toast(id = id, message = message, type = ToastType.Normal, duration = 3000.milliseconds)
    }
}
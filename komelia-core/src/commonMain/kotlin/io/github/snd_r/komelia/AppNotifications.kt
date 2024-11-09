package io.github.snd_r.komelia

import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotification.Error
import io.github.snd_r.komelia.AppNotification.Normal
import io.github.snd_r.komelia.AppNotification.Success
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
import snd.komf.client.toKomfErrorResponse
import snd.komga.client.common.toErrorResponse
import snd.komga.client.common.toViolationResponse
import kotlin.time.Duration.Companion.seconds

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

    fun <R> runCatchingToNotifications(
        coroutineScope: CoroutineScope,
        onFailure: (exception: Throwable) -> Unit = {},
        onSuccess: (value: R) -> Unit = {},
        block: suspend () -> R,
    ) {
        coroutineScope.launch {
            runCatchingToNotifications { block() }
                .onFailure(onFailure)
                .onSuccess(onSuccess)
        }
    }

    suspend inline fun <R> runCatchingToNotifications(block: () -> R): Result<R> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            KotlinLogging.logger {}.warn(e) {}
            throw e
        } catch (exception: Throwable) {
            addErrorNotification(exception)
            return Result.failure(exception)
        }
    }

    suspend fun addErrorNotification(exception: Throwable) {
        KotlinLogging.logger {}.catching(exception)
        when (exception) {
            is CancellationException -> {}
            is ResponseException -> toErrorNotification(exception)
            else -> add(Error(exception.message ?: exception.cause?.message ?: "Unknown error"))
        }
    }

    private suspend fun toErrorNotification(exception: ResponseException) {
        val contentType = exception.response.contentType()
        contentType?.toString()
        val errorMessage =
            if (contentType != null && contentType.contentType == "application" && contentType.contentSubtype == "json") {
                parseJsonErrorMessage(exception)
            } else {
                errorMessageFromStatusCode(exception.response.status)
            }

        add(Error(errorMessage))
    }
}

private suspend fun parseJsonErrorMessage(exception: ResponseException): String {
    return exception.toErrorResponse()?.message
        ?: exception.toKomfErrorResponse()?.message
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

sealed class AppNotification(val id: Long = Clock.System.now().toEpochMilliseconds()) {
    class Success(val message: String) : AppNotification()
    class Normal(val message: String) : AppNotification()
    class Error(val message: String) : AppNotification()
}


fun AppNotification.toToast(): Toast {
    return when (this) {
        is Error -> Toast(id = id, message = message, type = ToastType.Error, duration = 5.seconds)
        is Success -> Toast(id = id, message = message, type = ToastType.Success, duration = 4.seconds)
        is Normal -> Toast(id = id, message = message, type = ToastType.Normal, duration = 3.seconds)
    }
}
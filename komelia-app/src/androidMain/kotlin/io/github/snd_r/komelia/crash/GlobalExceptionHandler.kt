package io.github.snd_r.komelia.crash

import android.content.Context
import android.content.Intent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class GlobalExceptionHandler private constructor(
    private val applicationContext: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler,
) : Thread.UncaughtExceptionHandler {

    @Serializable
    data class ExceptionData(
        val exceptionName: String,
        val message: String?,
        val stacktrace: String
    )

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            logger.catching(exception)
            val exceptionData = ExceptionData(
                exceptionName = exception::class.simpleName ?: "Unknown Error",
                message = exception.message,
                stacktrace = exception.stackTraceToString()
            )
            val intent = Intent(applicationContext, CrashActivity::class.java).apply {
                putExtra(INTENT_EXTRA, Json.encodeToString(exceptionData))
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            applicationContext.startActivity(intent)
            exitProcess(0)
        } catch (e: Exception) {
            defaultHandler.uncaughtException(thread, exception)
        }
    }

    companion object {
        private const val INTENT_EXTRA = "ExceptionData"

        fun initialize(applicationContext: Context) {
            val handler = GlobalExceptionHandler(
                applicationContext,
                Thread.getDefaultUncaughtExceptionHandler() as Thread.UncaughtExceptionHandler,
            )
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }

        fun getExceptionDataFromIntent(intent: Intent): ExceptionData? {
            return try {
                Json.decodeFromString<ExceptionData>(intent.getStringExtra(INTENT_EXTRA)!!)
            } catch (e: Exception) {
                logger.error { "Wasn't able to retrieve throwable from intent" }
                null
            }
        }
    }
}

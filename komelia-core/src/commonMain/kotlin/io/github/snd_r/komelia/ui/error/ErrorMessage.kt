package io.github.snd_r.komelia.ui.error

fun formatExceptionMessage(exception: Throwable): String {
    return buildString {
        exception::class.simpleName?.let { append("$it: ") }
        exception.message?.let { append("$it; ") }
        var cause = exception.cause
        while (cause != null) {
            cause.message?.let { append("$it;") }
            cause = cause.cause
        }
    }
}

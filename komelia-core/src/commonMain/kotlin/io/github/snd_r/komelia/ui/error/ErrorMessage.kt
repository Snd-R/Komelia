package io.github.snd_r.komelia.ui.error

fun formatExceptionMessage(exception: Throwable): String {
    println("exception class ${exception::class.simpleName}")
    println("exception cause ${exception.cause}")
    println("exception cause message ${exception.cause?.message}")
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

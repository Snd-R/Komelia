package snd.komelia.ui

import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import snd.komelia.AppNotification
import kotlin.time.Duration.Companion.seconds

fun AppNotification.toToast(): Toast {
    return when (this) {
        is AppNotification.Error -> Toast(id = id, message = message, type = ToastType.Error, duration = 5.seconds)
        is AppNotification.Success -> Toast(id = id, message = message, type = ToastType.Success, duration = 4.seconds)
        is AppNotification.Normal -> Toast(id = id, message = message, type = ToastType.Normal, duration = 3.seconds)
    }
}
package snd.komelia

import chrome.permissions.Permissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OriginSettingsViewModel {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val origins = MutableStateFlow<Collection<String>>(emptyList<String>())
    val newOriginError = MutableStateFlow<String?>(null)
    val allowedOriginsError = MutableStateFlow<String?>(null)

    init {
        coroutineScope.launch { runCatching { origins.value = getInjectionOrigins() } }

        coroutineScope.launch {
            allowedOriginsError.collectLatest {
                if (it != null) {
                    delay(5000)
                    allowedOriginsError.value = null
                }
            }
        }
    }

    fun onOriginAdd(origin: String) {
        val redactedOrigin = buildString {
            append(origin)
            if (!origin.endsWith("/")) append("/")
            if (!origin.endsWith('*')) append("*")
        }

        newOriginError.value = null
        coroutineScope.launch {
            runCatching {
                chrome.storage.local.set(OriginSettings(origins.value + redactedOrigin))
                chrome.permissions.request(scriptingPermissionObject(redactedOrigin)).await<JsAny>()
            }
                .onSuccess { origins.value = getInjectionOrigins() }
                .onFailure { newOriginError.value = it.message }
        }
    }

    fun onOriginRemove(origin: String) {
        coroutineScope.launch {
            runCatching { chrome.storage.local.set(OriginSettings(origins.value - origin)) }
                .onSuccess { origins.value = getInjectionOrigins() }
                .onFailure { allowedOriginsError.value = it.message }
        }
    }
}

private fun scriptingPermissionObject(origin: String): Permissions {
    js("return { permissions: ['scripting'], origins: [origin]};")
}


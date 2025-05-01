package snd.komelia

import chrome.permissions.Permissions
import kotlinx.coroutines.await
import snd.komelia.util.get
import snd.komelia.util.makeJsObject
import snd.komelia.util.set

external interface OriginSettings : JsAny {
    val origins: JsArray<JsString>
}

fun OriginSettings(origins: List<String>): OriginSettings {
    val jsObject = makeJsObject<OriginSettings>()
    jsObject["origins"] = origins.map { it.toJsString() }.toJsArray()
    return jsObject
}

suspend fun getInjectionOrigins(): Set<String> {
    val storedOrigins = getStoredOrigins()
    val allowedOrigins = getOriginsWithPermissions()
    val origins = storedOrigins.filter { stored ->
        allowedOrigins.any { allowed -> stored.startsWith(allowed.dropLast(1)) }
    }
    return origins.toSet()
}

suspend fun getOriginsWithPermissions(): List<String> {
    return chrome.permissions.getAll().await<Permissions>()
        .origins?.toList()?.map { it.toString() }
        ?: emptyList()
}

suspend fun getStoredOrigins(): List<String> {
    val stored = chrome.storage.local.get("origins").await<JsAny>()
    val origins = stored["origins"] as JsArray<*>?
    return origins?.toList()?.map { it.toString() } ?: emptyList()
}

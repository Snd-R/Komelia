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
        allowedOrigins.any { allowed -> originPatternCovers(allowed, stored) }
    }
    return origins.toSet()
}

// Browsers can normalize granted host permission patterns (drop port, change path),
// so plain string prefix comparison is not reliable. Compare scheme and host parts of match patterns instead
fun originPatternCovers(granted: String, stored: String): Boolean {
    if (granted == "<all_urls>") return true
    val grantedPattern = MatchPattern.parse(granted) ?: return false
    val storedPattern = MatchPattern.parse(stored) ?: return false

    val schemeMatches = grantedPattern.scheme == "*" && storedPattern.scheme in setOf("http", "https")
            || grantedPattern.scheme == storedPattern.scheme
    if (!schemeMatches) return false

    val grantedHost = grantedPattern.host
    val storedHost = storedPattern.host
    val hostMatches = when {
        grantedHost == "*" -> true
        grantedHost.startsWith("*.") -> {
            val domain = grantedHost.removePrefix("*.")
            storedHost == domain || storedHost.endsWith(".$domain")
        }

        else -> grantedHost == storedHost
    }
    if (!hostMatches) return false

    return grantedPattern.port == null || grantedPattern.port == "*" || grantedPattern.port == storedPattern.port
}

private data class MatchPattern(val scheme: String, val host: String, val port: String?) {
    companion object {
        fun parse(pattern: String): MatchPattern? {
            val schemeEnd = pattern.indexOf("://")
            if (schemeEnd <= 0) return null
            val scheme = pattern.substring(0, schemeEnd).lowercase()
            val authority = pattern.substring(schemeEnd + 3).substringBefore('/')
            if (authority.isEmpty()) return null

            // ipv6 hosts are enclosed in brackets [::1]:8080
            val portSeparator = if (authority.startsWith("[")) authority.indexOf(':', authority.indexOf(']'))
            else authority.lastIndexOf(':')
            val host = if (portSeparator == -1) authority else authority.substring(0, portSeparator)
            val port = if (portSeparator == -1) null else authority.substring(portSeparator + 1)
            return MatchPattern(scheme, host.lowercase(), port)
        }
    }
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

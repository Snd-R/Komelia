package snd.komelia

import chrome.scripting.RegisteredContentScript
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun main() {
    chrome.permissions.onAdded.addListener { permissions ->
        updateContentScripts()
    }
    chrome.permissions.onRemoved.addListener { permissions ->
        updateContentScripts()
    }
    chrome.storage.onChanged.addListener { changes, namespace ->
        updateContentScripts()
    }
    updateContentScripts()
}

private fun updateContentScripts() {
    coroutineScope.launch {
        runCatching {
            runCatching {
                chrome.scripting.unregisterContentScripts().await<JsAny>()
                println("unregistered current content script")
            }

            val injectionOrigins = getInjectionOrigins()
            if (injectionOrigins.isEmpty()) {
                println("injection origin list is empty. returning")
                return@runCatching
            }

            println("updating content scripts with origins $injectionOrigins")
            chrome.scripting.registerContentScripts(
                scripts = listOf(
                    RegisteredContentScript(
                        id = "komf-content",
                        js = listOf("content.js".toJsString()).toJsArray(),
                        matches = injectionOrigins.map { it.toJsString() }.toJsArray()
                    )
                ).toJsArray()
            ).await<JsAny>()
            println("updated content scripts $injectionOrigins")
        }.onFailure { println("failed to update content script ${it.message}") }
    }
}
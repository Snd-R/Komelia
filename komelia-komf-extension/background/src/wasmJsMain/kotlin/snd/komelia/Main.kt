package snd.komelia

import chrome.scripting.RegisteredContentScript
import chrome.scripting.ScriptInjection
import chrome.tabs.TabsQuery
import chrome.tabs.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

// permission and storage events usually fire together (popup saves origin and then requests permission).
// Updates are serialized to avoid concurrent unregister/register calls failing with duplicate script id
private val updateMutex = Mutex()

fun main() {
    chrome.permissions.onAdded.addListener { permissions ->
        updateContentScripts(injectIntoOpenTabs = true)
    }
    chrome.permissions.onRemoved.addListener { permissions ->
        updateContentScripts()
    }
    chrome.storage.onChanged.addListener { changes, namespace ->
        updateContentScripts()
    }
    updateContentScripts()
}

private fun updateContentScripts(injectIntoOpenTabs: Boolean = false) {
    coroutineScope.launch {
        updateMutex.withLock {
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

                if (injectIntoOpenTabs) injectIntoOpenTabs(injectionOrigins)
            }.onFailure { println("failed to update content script ${it.message}") }
        }
    }
}

// registered content scripts only run on page load.
// Inject into already open tabs so that page reload is not required after granting permission
private suspend fun injectIntoOpenTabs(origins: Set<String>) {
    val tabs = runCatching {
        chrome.tabs.query(TabsQuery(origins.map { it.toJsString() }.toJsArray())).await<JsArray<Tab>>()
    }.getOrElse {
        println("failed to query open tabs ${it.message}")
        return
    }

    tabs.toList().mapNotNull { it.id }.forEach { tabId ->
        runCatching {
            chrome.scripting.executeScript(
                ScriptInjection(tabId, listOf("content.js".toJsString()).toJsArray())
            ).await<JsAny>()
        }.onFailure { println("failed to inject content script into tab $tabId ${it.message}") }
    }
}

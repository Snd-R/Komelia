@file:JsQualifier("chrome.runtime")

package chrome.runtime

import kotlin.js.Promise

external var lastError: LastError? = definedExternally
external var id: String = definedExternally
external var onMessage: ExtensionMessageEvent = definedExternally
external var onMessageExternal: ExtensionMessageEvent = definedExternally

external interface LastError : JsAny {
    val message: String?
}

external interface MessageSender : JsAny {
    val id: String?

    //    var tab: chrome.tabs.Tab? get() = definedExternally; set(value) = definedExternally
    val frameId: Int?
    val url: String?
    val tlsChannelId: String?
}

external interface ExtensionMessageEvent : JsAny {
    fun addListener(callback: (message: JsAny, sender: MessageSender, sendResponse: (JsAny) -> Unit) -> Unit)
}

external fun sendMessage(message: JsAny): Promise<JsAny> = definedExternally

external fun sendMessage(
    extensionId: JsString?,
    message: JsAny,
    options: JsAny? = definedExternally
): Promise<JsAny> = definedExternally


external interface RuntimeEvent : JsAny {
    fun addListener(callback: () -> Unit)
}

external var onStartup: RuntimeEvent = definedExternally
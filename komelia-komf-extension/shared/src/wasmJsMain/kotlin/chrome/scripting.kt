@file:JsQualifier("chrome.scripting")

package chrome.scripting

import kotlin.js.Promise

external interface RegisteredContentScript : JsAny {
    val allFrames: JsBoolean?
    val css: JsArray<JsString>?
    val excludeMatches: JsArray<JsString>?
    val id: JsString
    val js: JsArray<JsString>?
    val matches: JsArray<JsString>?
    val matchOriginAsFallback: JsBoolean?
    val persistAcrossSessions: JsBoolean?
    val runAt: JsString?
    val world: JsString?
}

external fun registerContentScripts(scripts: JsArray<RegisteredContentScript>): Promise<JsAny> = definedExternally
external fun updateContentScripts(scripts: JsArray<RegisteredContentScript>): Promise<JsAny> = definedExternally
external fun unregisterContentScripts(scripts: JsArray<RegisteredContentScript> = definedExternally): Promise<JsAny> =
    definedExternally

@file:JsQualifier("chrome.tabs")

package chrome.tabs

import kotlin.js.Promise

external interface Tab : JsAny {
    val id: Int?
    val url: String?
}

external fun query(queryInfo: JsAny): Promise<JsArray<Tab>> = definedExternally

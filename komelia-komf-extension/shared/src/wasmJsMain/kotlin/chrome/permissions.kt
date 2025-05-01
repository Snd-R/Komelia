@file:JsQualifier("chrome.permissions")

package chrome.permissions

import kotlin.js.Promise

external interface Permissions : JsAny {
    var origins: JsArray<JsString>? get() = definedExternally; set(value) = definedExternally
    var permissions: JsArray<JsString>? get() = definedExternally; set(value) = definedExternally
}

external interface PermissionsRemovedEvent : JsAny {
    fun addListener(callback: (permissions: Permissions) -> Unit)
}

external interface PermissionsAddedEvent : JsAny {
    fun addListener(callback: (permissions: Permissions) -> Unit)
}

external fun contains(permissions: Permissions): Promise<JsBoolean> = definedExternally
external fun getAll(): Promise<Permissions> = definedExternally
external fun request(permissions: Permissions): Promise<JsBoolean> = definedExternally
external fun remove(permissions: Permissions): Promise<JsBoolean> = definedExternally

external var onRemoved: PermissionsRemovedEvent = definedExternally
external var onAdded: PermissionsAddedEvent = definedExternally

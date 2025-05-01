@file:JsQualifier("chrome.storage")

package chrome.storage

import chrome.permissions.Permissions
import kotlin.js.Promise

external interface StorageArea {
//    fun getBytesInUse(callback: (bytesInUse: JsNumber) -> Unit)
//    fun getBytesInUse(keys: String, callback: (bytesInUse: JsNumber) -> Unit)
//    fun getBytesInUse(keys: JsArray<JsString>, callback: (bytesInUse: JsNumber) -> Unit)
//    fun clear(callback: (() -> Unit)? = definedExternally /* null */)
//    fun set(items: JsAny, callback: (() -> Unit)? = definedExternally /* null */)
//    fun remove(keys: String, callback: (() -> Unit)? = definedExternally /* null */)
//    fun remove(keys: JsArray<JsString>, callback: (() -> Unit)? = definedExternally /* null */)
//    fun get(callback: (items: JsAny) -> Unit)
//    fun get(keys: String, callback: (items: JsAny) -> Unit)
//    fun get(keys: JsAny, callback: (items: JsAny) -> Unit)
//    fun get(keys: JsArray<JsString>, callback: (items: JsAny) -> Unit)

    fun get(keys: String): Promise<JsAny>
    fun remove(keys: String): Promise<JsAny>
    fun set(items: JsAny): Promise<JsAny>
}

external interface StorageChange {
    var newValue: JsAny? get() = definedExternally; set(value) = definedExternally
    var oldValue: JsAny? get() = definedExternally; set(value) = definedExternally
}

external interface LocalStorageArea : StorageArea {
    var QUOTA_BYTES: JsNumber
}

external interface SyncStorageArea : StorageArea {
    var MAX_SUSTAINED_WRITE_OPERATIONS_PER_MINUTE: JsNumber
    var QUOTA_BYTES: JsNumber
    var QUOTA_BYTES_PER_ITEM: JsNumber
    var MAX_ITEMS: JsNumber
    var MAX_WRITE_OPERATIONS_PER_HOUR: JsNumber
    var MAX_WRITE_OPERATIONS_PER_MINUTE: JsNumber
}

external interface StorageChangedEvent : JsAny {
    fun addListener(callback: (changes: JsAny, nameSpace: String) -> Unit)
}


external var local: LocalStorageArea = definedExternally
external var sync: SyncStorageArea = definedExternally
external var managed: StorageArea = definedExternally

external var onChanged: StorageChangedEvent = definedExternally
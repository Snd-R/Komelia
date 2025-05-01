package snd.komelia.messages

import snd.komelia.util.makeJsObject
import snd.komelia.util.set

external interface RemoveScriptOriginRequest : JsAny {
    val origin: String
}

external interface RemoveScriptOriginResponse : JsAny {
    val error: String?
}

fun RemoveScriptOriginRequest(origin: String): RemoveScriptOriginRequest {
    val jsObject = makeJsObject<RemoveScriptOriginRequest>()
    jsObject["type"] = ExtensionMessageType.REMOVE_SCRIPT_ORIGIN.name
    jsObject["origin"] = origin
    return jsObject
}

fun RemoveScriptOriginResponse(error: String?): RemoveScriptOriginResponse {
    val jsObject = makeJsObject<RemoveScriptOriginResponse>()
    jsObject["type"] = ExtensionMessageType.REMOVE_SCRIPT_ORIGIN.name
    error?.let { jsObject["error"] = it }
    return jsObject
}

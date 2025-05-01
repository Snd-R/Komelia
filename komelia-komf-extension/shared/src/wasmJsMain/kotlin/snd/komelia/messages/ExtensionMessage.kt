package snd.komelia.messages

external interface ExtensionMessage : JsAny {
    val type: String
}

fun ExtensionMessage.getType(): ExtensionMessageType = ExtensionMessageType.valueOf(type)

enum class ExtensionMessageType {
    REMOVE_SCRIPT_ORIGIN
}

package snd.komelia.ui.reader.image.continuous

import androidx.compose.ui.input.key.Key
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ContinuousKeyBindings(
    val bindings: Map<ContinuousShortcutAction, Set<Long>> = defaultBindings()
) {
    private val keyToAction: Map<Long, ContinuousShortcutAction> by lazy {
        bindings.flatMap { (action, keys) -> keys.map { it to action } }.toMap()
    }

    fun actionFor(key: Key): ContinuousShortcutAction? = keyToAction[key.keyCode]

    companion object {
        fun defaultBindings(): Map<ContinuousShortcutAction, Set<Long>> = mapOf(
            ContinuousShortcutAction.SCROLL_UP to setOf(Key.DirectionUp.keyCode),
            ContinuousShortcutAction.SCROLL_DOWN to setOf(Key.DirectionDown.keyCode)
        )
    }
}

fun ContinuousKeyBindings.toJson(): String = Json.encodeToString(this)

fun fromJsonToJson(json: String): ContinuousKeyBindings = try {
    Json.decodeFromString(json)
} catch (e: Exception) {
    ContinuousKeyBindings()
}

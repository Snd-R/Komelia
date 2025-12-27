package snd.komelia.offline.action

import kotlin.reflect.KClass

class OfflineActions(
    actionList: List<OfflineAction>,
) {
    private val actions: Map<KClass<out OfflineAction>, OfflineAction> = actionList.associateBy { it::class }

    fun <T : OfflineAction> get(type: KClass<out OfflineAction>): T {
        val action = actions[type]
            ?: throw IllegalStateException("Action ${type.qualifiedName} is not registered")

        @Suppress("UNCHECKED_CAST")
        return action as T

    }

    inline fun <reified T : OfflineAction> get(): T {
        return get(T::class)
    }
}
package world.gregs.voidps.engine.client.variable

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.event.EventDispatcher

open class Variables(
    private var events: EventDispatcher,
    val data: MutableMap<String, Any> = Object2ObjectOpenHashMap(2),
) {

    @Suppress("LeakingThis")
    var bits = VariableBits(this, events)

    @Suppress("UNCHECKED_CAST")
    open fun <T : Any> get(key: String): T? = data(key)[key] as? T

    open fun <T : Any> get(key: String, default: T): T = get(key) ?: default

    open fun <T : Any> getOrPut(key: String, block: () -> T): T {
        var value = get<T>(key)
        if (value != null) {
            return value
        }
        value = block.invoke()
        // Don't check if default or not as values must be set.
        data(key)[key] = value
        events.emit(VariableSet(key, null, value))
        return value
    }

    /**
     * Note: when a [PlayerVariables] is set to its default value it will be cleared and [contains] will return false.
     */
    open fun contains(key: String): Boolean = data(key).containsKey(key)

    open fun set(key: String, value: Any, refresh: Boolean = true) {
        val previous: Any? = get(key)
        if (previous == value) {
            return
        }
        data(key)[key] = value
        if (refresh) {
            send(key)
        }
        events.emit(VariableSet(key, previous, value))
    }

    open fun clear(key: String, refresh: Boolean = true): Any? {
        val removed = data(key).remove(key)
        if (refresh) {
            send(key)
        }
        events.emit(VariableSet(key, removed ?: return null, null))
        return removed
    }

    open fun send(key: String) {
    }

    open fun data(key: String): MutableMap<String, Any> = data
}

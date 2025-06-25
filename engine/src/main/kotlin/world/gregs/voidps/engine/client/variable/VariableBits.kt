package world.gregs.voidps.engine.client.variable

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.event.EventDispatcher

class VariableBits(
    private val variables: Variables,
    private val events: EventDispatcher
) {

    fun contains(key: String, id: Any): Boolean {
        val value: List<Any> = variables.get(key) ?: return false
        return value.contains(id)
    }

    fun set(key: String, value: Any, refresh: Boolean): Boolean {
        val values: MutableList<Any> = variables.getOrPut(key) { ObjectArrayList<Any>().apply { add(value) } }
        if (!values.contains(value) && values.add(value)) {
            if (refresh) {
                variables.send(key)
            }
            events.emit(VariableBitAdded(key, value))
            return true
        }
        return false
    }

    fun remove(key: String, value: Any, refresh: Boolean): Boolean {
        val values: MutableList<Any> = variables.get(key) ?: return false
        if (values.remove(value)) {
            if (refresh) {
                variables.send(key)
            }
            events.emit(VariableBitRemoved(key, value))
            return true
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    fun clear(key: String, refresh: Boolean) {
        val values = variables.clear(key, refresh) as? List<Any> ?: return
        if (refresh) {
            variables.send(key)
        }
        for (value in values) {
            events.emit(VariableBitRemoved(key, value))

        }
    }
}
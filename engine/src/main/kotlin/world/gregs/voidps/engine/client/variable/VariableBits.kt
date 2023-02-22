package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Events

class VariableBits(
    private val variables: Variables,
    private val events: Events
) {

    fun contains(key: String, id: Any): Boolean {
        val value: ArrayList<Any> = variables.getOrNull(key) ?: return false
        return value.contains(id)
    }

    fun set(key: String, value: Any, refresh: Boolean) {
        val values: ArrayList<Any> = variables.getOrPut(key) { arrayListOf(value) }
        if (!values.contains(value) && values.add(value)) {
            if (refresh) {
                variables.send(key)
            }
            events.emit(VariableAdded(key, value))
        }
    }

    fun remove(key: String, value: Any, refresh: Boolean) {
        val values: ArrayList<Any> = variables.getOrNull(key) ?: return
        if (values.remove(value)) {
            if (refresh) {
                variables.send(key)
            }
            events.emit(VariableRemoved(key, value))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun clear(key: String, refresh: Boolean) {
        val values = variables.clear(key, refresh) as? ArrayList<Any> ?: return
        if (refresh) {
            variables.send(key)
        }
        for (value in values) {
            events.emit(VariableRemoved(key, value))
        }
    }
}
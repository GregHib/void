package world.gregs.voidps.engine.event.handle

sealed class EventField {
    data class Event(val type: String) : EventField() {
        override fun get(data: Map<String, Any>): Set<Any> {
            return if (data["approach"] == true) setOf(type.replace("operate", "approach")) else setOf(type)
        }
    }
    data class StringKey(val key: String) : EventField() {
        override fun get(data: Map<String, Any>): Set<Any> = setOf(data[key] as String)
    }
    data class StringList(val key: String) : EventField() {
        override fun get(data: Map<String, Any>): Set<Any> {
            val ids = data[key] as? List<String>
            return if (ids.isNullOrEmpty()) setOf("*") else ids.toSet()
        }
    }
    data class StaticString(val value: String) : EventField() {
        override fun get(data: Map<String, Any>): Set<Any> = setOf(value)
    }
    data class StaticSet(val value: Set<String>) : EventField() {
        override fun get(data: Map<String, Any>): Set<Any> = value
    }
    data class ListIndex(val key: String, val index: Int) : EventField() {
        override fun get(data: Map<String, Any>): Set<Any> {
            val targets = data[key] as? List<String>
            return if (targets.isNullOrEmpty()) setOf("*") else setOf(targets[index])
        }
    }
    abstract fun get(data: Map<String, Any>): Set<Any>
}
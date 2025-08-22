package world.gregs.voidps.engine.event

sealed class EventField {
    data class Event(val type: String) : EventField() {
        override fun get(data: UseData): Set<Any> = if (data.approach) setOf(type.replace("operate", "approach")) else setOf(type)
    }
    data object Option : EventField() {
        override fun get(data: UseData): Set<Any> = setOf(data.option)
    }
    data object Ids : EventField() {
        override fun get(data: UseData): Set<Any> = if (data.sources.isEmpty()) setOf("*") else data.sources
    }
    data object Component : EventField() {
        override fun get(data: UseData): Set<Any> = setOf(data.component)
    }
    data object On : EventField() {
        override fun get(data: UseData): Set<Any> = if (data.targets.isEmpty()) setOf("*") else data.targets
    }
    data object Player : EventField() {
        override fun get(data: UseData): Set<Any> = setOf("player")
    }
    data object Npc : EventField() {
        override fun get(data: UseData): Set<Any> = if (data.targets.isEmpty()) setOf("*") else data.targets
    }
    abstract fun get(data: UseData): Set<Any>
}
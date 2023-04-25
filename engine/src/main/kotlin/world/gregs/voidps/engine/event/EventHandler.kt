package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import kotlin.reflect.KClass

data class EventHandler(
    val event: KClass<out Event>,
    val condition: Event.(Entity) -> Boolean = { true },
    val priority: Priority = Priority.MEDIUM,
    val block: suspend Event.(Entity) -> Unit = {}
) : Comparable<EventHandler> {
    override fun compareTo(other: EventHandler): Int {
        return other.priority.ordinal.compareTo(priority.ordinal)
    }
}
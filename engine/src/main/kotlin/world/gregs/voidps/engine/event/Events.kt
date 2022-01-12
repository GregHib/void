package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import java.util.*
import kotlin.reflect.KClass

class Events(
    private val entity: Entity,
    private val events: MutableMap<KClass<out Event>, PriorityQueue<EventHandler>> = mutableMapOf()
) : MutableMap<KClass<out Event>, PriorityQueue<EventHandler>> by events {

    var all: ((Event) -> Unit)? = null

    fun addAll(clazz: KClass<out Event>, values: List<EventHandler>) {
        events.getOrPut(clazz) { emptyContainer() }.addAll(values)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Entity, reified E : Event> on(
        noinline condition: E.(T) -> Boolean = { true },
        priority: Priority = Priority.MEDIUM,
        noinline block: E.(T) -> Unit
    ): EventHandler {
        val handler = EventHandler(E::class, condition as Event.(Entity) -> Boolean, priority, block as Event.(Entity) -> Unit)
        !getOrPut(E::class) { emptyContainer() }.add(handler)
        return handler
    }

    fun remove(handler: EventHandler) {
        events[handler.event]?.remove(handler)
    }

    fun <E : Event> emit(event: E): Boolean {
        all?.invoke(event)
        var called = false
        events[event::class]
            ?.sortedByDescending { it.priority }
            ?.forEach {
                if (event is CancellableEvent && event.cancelled) {
                    return true
                }
                if (it.condition(event, entity)) {
                    called = true
                    it.block(event, entity)
                }
            }
        return called
    }

    companion object {
        fun emptyContainer(): PriorityQueue<EventHandler> {
            return PriorityQueue { one, two -> two.priority.ordinal.compareTo(one.priority.ordinal) }
        }
    }
}

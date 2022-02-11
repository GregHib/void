package world.gregs.voidps.engine.event

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.Entity
import kotlin.reflect.KClass

class Events(
    private val entity: Entity,
    private val events: MutableMap<KClass<out Event>, MutableList<EventHandler>> = mutableMapOf()
) : MutableMap<KClass<out Event>, MutableList<EventHandler>> by events {

    var all: ((Event) -> Unit)? = null

    fun addAll(clazz: KClass<out Event>, values: List<EventHandler>) {
        val list = events.getOrPut(clazz) { ObjectArrayList() }
        list.addAll(values)
        events[clazz] = list.sortedByDescending { it.priority.ordinal }.toMutableList()
    }

    fun add(clazz: KClass<out Event>, value: EventHandler) {
        val list = events.getOrPut(clazz) { ObjectArrayList() }
        list.add(value)
        events[clazz] = list.sortedByDescending { it.priority.ordinal }.toMutableList()
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Entity, reified E : Event> on(
        noinline condition: E.(T) -> Boolean = { true },
        priority: Priority = Priority.MEDIUM,
        noinline block: E.(T) -> Unit
    ): EventHandler {
        val handler = EventHandler(E::class, condition as Event.(Entity) -> Boolean, priority, block as Event.(Entity) -> Unit)
        add(E::class, handler)
        return handler
    }

    fun remove(handler: EventHandler) {
        events[handler.event]?.remove(handler)
    }

    fun <E : Event> emit(event: E): Boolean {
        all?.invoke(event)
        var called = false
        events[event::class]
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
}

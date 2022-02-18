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

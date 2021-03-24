package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.reflect.KClass

class Events(
    private val entity: Entity,
    private val events: MutableMap<KClass<out Event>, MutableList<EventHandler>> = mutableMapOf()
) : MutableMap<KClass<out Event>, MutableList<EventHandler>> by events {

    fun addAll(clazz: KClass<out Event>, values: List<EventHandler>) {
        events.getOrPut(clazz) { mutableListOf() }.addAll(values)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified E : Event> on(
        noinline condition: E.(Player) -> Boolean = { true },
        noinline block: E.(Player) -> Unit
    ) {
        getOrPut(E::class) { mutableListOf() }.add(EventHandler(E::class, condition as Event.(Entity) -> Boolean, block as Event.(Entity) -> Unit))
    }

    fun <E : Event> emit(event: E) = events[event::class]
        ?.filter { it.condition(event, entity) }
        ?.forEach { it.block(event, entity) }
}

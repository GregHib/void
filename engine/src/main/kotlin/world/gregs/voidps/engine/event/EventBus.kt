package world.gregs.voidps.engine.event

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.utility.get
import kotlin.reflect.KClass

val eventModule = module {
    single { EventBus() }
}

class EventAction(
    val event: KClass<out Event>,
    val condition: Event.(Entity) -> Boolean,
    val block: Event.(Entity) -> Unit
)

/**
 * Handles the publication of [Event]s; [emit] to subscribers; [EventHandler].
 * Note: [EventHandler]'s are stored in a highest-first prioritised chain
 * @author GregHib <greg@gregs.world>
 * @since March 26, 2020
 */
@Suppress("UNCHECKED_CAST")
class EventBus {

    private val handlers = mutableMapOf<KClass<*>, EventHandler<*>>()
    val map = mutableMapOf<KClass<out Entity>, MutableMap<KClass<out Event>, MutableList<EventAction>>>()

    /**
     * Attaches [handler] to the handler chain
     */
    fun <E : Event> add(clazz: KClass<E>?, handler: EventHandler<E>) {
        checkNotNull(clazz) { "Event must have a companion object." }
        var last = get(clazz)
        var next: EventHandler<E>?

        while (last != null) {
            next = last.next
            if (next == null) {
                // Append
                last.next = handler
                break
            }
            last = next
        }

        if (last == null) {
            handlers[clazz] = handler
        }
    }

    /**
     * Clears all handlers
     */
    fun clear() {
        handlers.clear()
    }

    /**
     * Returns [EventHandler] with matching [clazz]
     */
    fun <E : Event> get(clazz: KClass<E>): EventHandler<E>? {
        return handlers[clazz] as? EventHandler<E>
    }

    /**
     * Event's are only emitted to handlers which are applicable according to [EventHandler.applies]
     */
    fun <E : Event> emit(event: E, clazz: KClass<E>) {
        if (!checkPassed(event, clazz)) {
            return
        }

        var handler = get(clazz)
        while (handler != null) {

            if (handler.applies(event)) {
                try {
                    handler.invoke(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            handler = handler.next
        }
    }

    /**
     * An event must have at least one successful [EventHandler.checked] for any applicable handler to be emitted.
     */
    private fun <E : Event> checkPassed(event: E, clazz: KClass<E>): Boolean {
        var handler = get(clazz)

        while (handler != null) {
            if (handler.applies(event)) {
                return true
            }
            handler = handler.next
        }
        return false
    }

    /**
     * Helper function for emitting events
     */
    inline fun <reified E : Event> emit(event: E) = emit(event, E::class)

    fun <T : Entity> populate(entity: T) {
        for ((key, values) in get(entity::class)) {
            entity.events.addAll(key, values)
        }
    }

    fun get(entity: KClass<out Entity>): Map<KClass<out Event>, List<EventAction>> {
        return map[entity] ?: emptyMap()
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Entity, reified E : Event> on(noinline condition: E.(T) -> Boolean = { true }, noinline block: E.(T) -> Unit) {
    get<EventBus>().map.getOrPut(T::class) { mutableMapOf() }.getOrPut(E::class) { mutableListOf() }.add(EventAction(E::class, condition as Event.(Entity) -> Boolean, block as Event.(Entity) -> Unit))
}

@JvmName("onPlayer")
inline fun <reified E : Event> on(noinline condition: E.(Player) -> Boolean = { true }, noinline block: E.(Player) -> Unit) = on<Player, E>(condition, block)

@JvmName("onNPC")
inline fun <reified E : Event> on(noinline condition: E.(NPC) -> Boolean = { true }, noinline block: E.(NPC) -> Unit) = on<NPC, E>(condition, block)

@JvmName("onItem")
inline fun <reified E : Event> on(noinline condition: E.(FloorItem) -> Boolean = { true }, noinline block: E.(FloorItem) -> Unit) = on<FloorItem, E>(condition, block)

@JvmName("onWorld")
inline fun <reified E : Event> on(noinline condition: E.(World) -> Boolean = { true }, noinline block: E.(World) -> Unit) = on<World, E>(condition, block)
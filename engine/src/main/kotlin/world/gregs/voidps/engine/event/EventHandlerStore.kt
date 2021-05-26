package world.gregs.voidps.engine.event

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.utility.get
import kotlin.reflect.KClass

val eventModule = module {
    single { EventHandlerStore() }
}

/**
 * Handles the storage and delivery of global [EventHandler]'s
 */
class EventHandlerStore {

    private val handlers = mutableMapOf<KClass<out Entity>, MutableMap<KClass<out Event>, MutableList<EventHandler>>>()

    fun <T : Entity> populate(clazz: KClass<T>, events: Events) {
        for ((key, values) in get(clazz)) {
            events.addAll(key, values)
        }
    }

    fun <T : Entity> populate(entity: T) = populate(entity::class, entity.events)

    fun get(entity: KClass<out Entity>): Map<KClass<out Event>, List<EventHandler>> {
        return handlers[entity] ?: emptyMap()
    }

    fun add(entity: KClass<out Entity>, event: KClass<out Event>, condition: Event.(Entity) -> Boolean, block: Event.(Entity) -> Unit) {
        handlers.getOrPut(entity) { mutableMapOf() }.getOrPut(event) { mutableListOf() }.add(EventHandler(event, condition, block))
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Entity, reified E : Event> on(noinline condition: E.(T) -> Boolean = { true }, noinline block: E.(T) -> Unit) {
    get<EventHandlerStore>().add(T::class, E::class, condition as Event.(Entity) -> Boolean, block as Event.(Entity) -> Unit)
}

@JvmName("onPlayer")
inline fun <reified E : Event> on(noinline condition: E.(Player) -> Boolean = { true }, noinline block: E.(Player) -> Unit) = on<Player, E>(condition, block)

@JvmName("onNPC")
inline fun <reified E : Event> on(noinline condition: E.(NPC) -> Boolean = { true }, noinline block: E.(NPC) -> Unit) = on<NPC, E>(condition, block)

@JvmName("onItem")
inline fun <reified E : Event> on(noinline condition: E.(FloorItem) -> Boolean = { true }, noinline block: E.(FloorItem) -> Unit) = on<FloorItem, E>(condition, block)

@JvmName("onWorld")
inline fun <reified E : Event> on(noinline condition: E.(World) -> Boolean = { true }, noinline block: E.(World) -> Unit) = on<World, E>(condition, block)

@JvmName("onBot")
inline fun <reified E : Event> on(noinline condition: E.(Bot) -> Boolean = { true }, noinline block: E.(Bot) -> Unit) = on<Bot, E>(condition, block)
package world.gregs.voidps.engine.event

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.get
import kotlin.reflect.KClass

/**
 * Handles the storage and delivery of global [EventHandler]'s
 */
class EventHandlerStore {

    private val handlers = Object2ObjectOpenHashMap<KClass<out EventDispatcher>, MutableMap<KClass<out Event>, MutableList<EventHandler>>>()

    private val parents = Object2ObjectOpenHashMap(mapOf<KClass<out EventDispatcher>, List<KClass<out EventDispatcher>>>(
        EventDispatcher::class to listOf(World::class, FloorItem::class, Character::class),
        Character::class to listOf(Player::class, NPC::class)
    ))

    fun populate(clazz: KClass<out EventDispatcher>, events: Events) {
        events.set(get(clazz))
    }

    fun <T : EventDispatcher> populate(entity: T) {
        populate(entity::class, entity.events)
    }

    fun get(entity: KClass<out EventDispatcher>): Map<KClass<out Event>, MutableList<EventHandler>> {
        return handlers[entity] ?: emptyMap()
    }

    fun add(entity: KClass<out EventDispatcher>, event: KClass<out Event>, handler: EventHandler) {
        val list = handlers.getOrPut(entity) { Object2ObjectOpenHashMap(2) }.getOrPut(event) { mutableListOf() }
        list.add(handler)
        list.sort()
    }

    fun add(dispatcher: KClass<out EventDispatcher>, event: KClass<out Event>, condition: Event.(EventDispatcher) -> Boolean, priority: Priority, block: suspend Event.(EventDispatcher) -> Unit) {
        add(dispatcher, event, EventHandler(event, condition, priority, block))
        for (parent in parents[dispatcher] ?: return) {
            add(parent, event, EventHandler(event, condition, priority, block))
        }
    }

    fun clear() {
        handlers.clear()
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : EventDispatcher, reified E : Event> addEvent(noinline condition: E.(T) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(T) -> Unit) {
    get<EventHandlerStore>().add(T::class, E::class, condition as Event.(EventDispatcher) -> Boolean, priority, block as suspend Event.(EventDispatcher) -> Unit)
}

inline fun <reified E : Event> on(noinline condition: E.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(Player) -> Unit) =
    addEvent(condition, priority, block)

inline fun <reified E : Event> onNPC(noinline condition: E.(NPC) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(NPC) -> Unit) =
    addEvent(condition, priority, block)

inline fun <reified E : Event> onCharacter(noinline condition: E.(Character) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(Character) -> Unit) =
    addEvent(condition, priority, block)

inline fun <reified E : Event> onFloorItem(noinline condition: E.(FloorItem) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(FloorItem) -> Unit) =
    addEvent(condition, priority, block)

inline fun <reified E : Event> onWorld(noinline condition: E.(World) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(World) -> Unit) =
    addEvent(condition, priority, block)
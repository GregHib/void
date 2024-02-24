package world.gregs.voidps.engine.event

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.get
import kotlin.reflect.KClass

private val parents = Object2ObjectOpenHashMap(mapOf<KClass<out EventDispatcher>, List<KClass<out EventDispatcher>>>(
    EventDispatcher::class to listOf(World::class, FloorItem::class, Character::class),
    Character::class to listOf(Player::class, NPC::class)
))

@Suppress("UNCHECKED_CAST")
inline fun <reified T : EventDispatcher, reified E : Event> addEvent(noinline condition: E.(T) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(T) -> Unit) {
    add(T::class, E::class, condition as Event.(EventDispatcher) -> Boolean, priority, block as suspend Event.(EventDispatcher) -> Unit)
}

fun add(dispatcher: KClass<out EventDispatcher>, event: KClass<out Event>, condition: Event.(EventDispatcher) -> Boolean, priority: Priority, block: suspend Event.(EventDispatcher) -> Unit) {
    val handler = EventHandler(event, condition, priority, block)
    EventStore.events.add(dispatcher, event, handler)
    for (parent in parents[dispatcher] ?: return) {
        EventStore.events.add(parent, event, handler)
    }
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
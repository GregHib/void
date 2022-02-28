package world.gregs.voidps.engine.event

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.proj.Projectile
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.utility.get
import kotlin.reflect.KClass

val eventModule = module {
    single { EventHandlerStore() }
}

/**
 * Handles the storage and delivery of global [EventHandler]'s
 */
class EventHandlerStore {

    private val handlers = mutableMapOf<KClass<out Entity>, MutableMap<KClass<out Event>, MutableList<EventHandler>>>()

    private val parents = mapOf(
        Entity::class to listOf(World::class, AreaGraphic::class, FloorItem::class, GameObject::class, Projectile::class, AreaSound::class, Character::class),
        Character::class to listOf(Player::class, NPC::class)
    )

    fun populate(clazz: KClass<out Entity>, events: Events) {
        events.set(get(clazz))
    }

    fun <T : Entity> populate(entity: T) {
        populate(entity::class, entity.events)
    }

    fun get(entity: KClass<out Entity>): Map<KClass<out Event>, MutableList<EventHandler>> {
        return handlers[entity] ?: emptyMap()
    }

    fun add(entity: KClass<out Entity>, event: KClass<out Event>, handler: EventHandler) {
        val list = handlers.getOrPut(entity) { mutableMapOf() }.getOrPut(event) { mutableListOf() }
        list.add(handler)
        list.sort()
    }

    fun add(entity: KClass<out Entity>, event: KClass<out Event>, condition: Event.(Entity) -> Boolean, priority: Priority, block: Event.(Entity) -> Unit) {
        add(entity, event, EventHandler(event, condition, priority, block))
        for (parent in parents[entity] ?: return) {
            add(parent, event, EventHandler(event, condition, priority, block))
        }
    }

    fun clear() {
        handlers.clear()
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Entity, reified E : Event> on(noinline condition: E.(T) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(T) -> Unit) {
    get<EventHandlerStore>().add(T::class, E::class, condition as Event.(Entity) -> Boolean, priority, block as Event.(Entity) -> Unit)
}

@JvmName("onPlayer")
inline fun <reified E : Event> on(noinline condition: E.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(Player) -> Unit) = on<Player, E>(condition, priority, block)

@JvmName("onNPC")
inline fun <reified E : Event> on(noinline condition: E.(NPC) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(NPC) -> Unit) = on<NPC, E>(condition, priority, block)

@JvmName("onCharacter")
inline fun <reified E : Event> on(noinline condition: E.(Character) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(Character) -> Unit) = on<Character, E>(condition, priority, block)

@JvmName("onItem")
inline fun <reified E : Event> on(noinline condition: E.(FloorItem) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(FloorItem) -> Unit) = on<FloorItem, E>(condition, priority, block)

@JvmName("onWorld")
inline fun <reified E : Event> on(noinline condition: E.(World) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(World) -> Unit) = on<World, E>(condition, priority, block)

@JvmName("onBot")
inline fun <reified E : Event> on(noinline condition: E.(Bot) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(Bot) -> Unit) = on<Bot, E>(condition, priority, block)

@JvmName("onObject")
inline fun <reified E : Event> on(noinline condition: E.(GameObject) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: E.(GameObject) -> Unit) = on<GameObject, E>(condition, priority, block)
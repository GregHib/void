package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class EventStore : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined + errorHandler
    private val handlers: MutableMap<String, MutableList<EventHandler>> = Object2ObjectOpenHashMap()
    var all: ((Player, Event) -> Unit)? = null

    fun add(dispatcher: KClass<out EventDispatcher>, event: KClass<out Event>, handler: EventHandler) {
        handlers.getOrPut("${dispatcher.simpleName}_${event.simpleName}") { mutableListOf() }.add(handler)
        for (parent in parents[dispatcher] ?: return) {
            handlers.getOrPut("${parent}_${event.simpleName}") { mutableListOf() }.add(handler)
        }
    }

    fun init() {
        for ((_, list) in handlers) {
            list.sort()
        }
    }

    fun clear() {
        handlers.clear()
    }

    fun <E : Event> emit(dispatcher: EventDispatcher, event: E): Boolean {
        val handlers = handlers["${dispatcher::class.simpleName}_${event::class.simpleName}"] ?: return false
        if (dispatcher is Player && dispatcher.contains("bot")) {
            all?.invoke(dispatcher, event)
        }
        var called = false
        for (handler in handlers) {
            if (event is CancellableEvent && event.cancelled) {
                return true
            }
            if (handler.condition(event, dispatcher)) {
                called = true
                runBlocking {
                    handler.block(event, dispatcher)
                }
            }
        }
        return called
    }

    fun <E : SuspendableEvent> emit(dispatcher: EventDispatcher, event: E): Boolean {
        val handlers = handlers["${dispatcher::class.simpleName}_${event::class.simpleName}"] ?: return false
        if (handlers.none { it.condition(event, dispatcher) }) {
            return false
        }
        if (dispatcher is Player && dispatcher.contains("bot")) {
            all?.invoke(dispatcher, event)
        }
        launch {
            for (handler in handlers) {
                if (event is CancellableEvent && event.cancelled) {
                    return@launch
                }
                if (handler.condition(event, dispatcher)) {
                    handler.block(event, dispatcher)
                }
            }
        }
        return true
    }

    fun <E : SuspendableEvent> contains(dispatcher: EventDispatcher, event: E): Boolean {
        val eventHandlers = handlers["${dispatcher::class.simpleName}_${event::class.simpleName}"]
        return eventHandlers != null && eventHandlers.any { it.condition(event, dispatcher) }
    }

    companion object {
        private val logger = InlineLogger()
        private val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                logger.warn(throwable) { "Error in event." }
            }
        }
        var events = EventStore()
            private set

        fun setEvents(eventStore: EventStore) {
            this.events = eventStore
        }

        private val parents = Object2ObjectOpenHashMap(mapOf(
            Character::class to listOf(Player::class.simpleName, NPC::class.simpleName)
        ))
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : EventDispatcher, reified E : Event> addEvent(noinline condition: E.(T) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(T) -> Unit) {
    val dispatcher = T::class
    val event = E::class
    val handler = EventHandler(condition as Event.(EventDispatcher) -> Boolean, priority, block as suspend Event.(EventDispatcher) -> Unit)
    EventStore.events.add(dispatcher, event, handler)
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

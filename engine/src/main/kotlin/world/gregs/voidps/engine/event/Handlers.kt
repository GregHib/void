package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import kotlin.coroutines.CoroutineContext

class Handlers : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined + errorHandler
    private val handlers = Object2ObjectOpenHashMap<String, suspend Event.(EventDispatcher) -> Unit>()
    private val subscribers = Object2ObjectOpenHashMap<String, MutableSet<suspend Event.(EventDispatcher) -> Unit>>()

    fun add(key: String, handler: suspend Event.(EventDispatcher) -> Unit) {
        if (handlers.containsKey(key)) {
            throw IllegalArgumentException("Handler already exists for: ${key}.")
        }
        handlers[key] = handler
    }

    fun subscribe(key: String, handler: suspend Event.(EventDispatcher) -> Unit) {
        subscribers.getOrPut(key) { mutableSetOf() }.add(handler)
    }

    fun send(dispatcher: EventDispatcher, event: Event): Unit = runBlocking {
        val key = "${key(dispatcher)}_${event.key()}"
        val subscribers = subscribers[key]
        if (subscribers != null) {
            for (subscriber in subscribers) {
                subscriber.invoke(event, dispatcher)
                if (event is CancellableEvent && event.cancelled) {
                    return@runBlocking
                }
            }
        }
        handlers[key]?.invoke(event, dispatcher)
    }

    fun send(dispatcher: EventDispatcher, event: SuspendableEvent) {
        val key = "${key(dispatcher)}_${event.key()}"
        val handler = handlers[key]
        val subscribers = subscribers[key]
        if (handler != null || subscribers != null) {
            launch {
                if (subscribers != null) {
                    for (subscriber in subscribers) {
                        subscriber.invoke(event, dispatcher)
                        if (event is CancellableEvent && event.cancelled) {
                            return@launch
                        }
                    }
                }
                handler?.invoke(event, dispatcher)
            }
        }
    }

    fun contains(key: String): Boolean {
        return handlers.containsKey(key) || subscribers.containsKey(key)
    }

    fun clear() {
        handlers.clear()
        subscribers.clear()
    }

    companion object {
        private val logger = InlineLogger()
        private val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                logger.warn(throwable) { "Error in event." }
            }
        }
        private fun key(dispatcher: EventDispatcher): String {
            return when(dispatcher) {
                is Player -> HandlerType.PLAYER
                is NPC -> "${HandlerType.NPC}${dispatcher.id}"
                is FloorItem -> "${HandlerType.FLOOR_ITEM}${dispatcher.id}"
                is GameObject -> "${HandlerType.OBJECT}${dispatcher.id}"
                is World -> HandlerType.WORLD
                else -> throw IllegalArgumentException("Unknown dispatcher: $dispatcher")
            }
        }
        val handlers = Handlers()
    }
}
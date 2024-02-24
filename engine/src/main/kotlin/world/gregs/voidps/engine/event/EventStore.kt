package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class EventStore : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined + errorHandler
    private val handlers: MutableMap<KClass<out EventDispatcher>, MutableMap<KClass<out Event>, MutableList<EventHandler>>> = Object2ObjectOpenHashMap()

    fun add(dispatcher: KClass<out EventDispatcher>, event: KClass<out Event>, handler: EventHandler) {
        handlers.getOrPut(dispatcher) { Object2ObjectOpenHashMap(2) }.getOrPut(event) { mutableListOf() }.add(handler)
    }

    fun init() {
        for ((_, map) in handlers) {
            for ((_, list) in map) {
                list.sort()
            }
        }
    }

    fun clear() {
        handlers.clear()
    }

    var all: ((Event) -> Unit)? = null

    fun <E : Event> emit(dispatcher: EventDispatcher, event: E): Boolean {
        val handlers = handlers[dispatcher::class]?.get(event::class) ?: return false
        all?.invoke(event)
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
        val handlers = handlers[dispatcher::class]?.get(event::class) ?: return false
        if (handlers.none { it.condition(event, dispatcher) }) {
            return false
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
        val eventHandlers = handlers[dispatcher::class]?.get(event::class)
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
    }
}
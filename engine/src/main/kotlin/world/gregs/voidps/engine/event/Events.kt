package world.gregs.voidps.engine.event

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class Events(
    private val dispatcher: EventDispatcher,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined + errorHandler
    private var events: Map<KClass<out Event>, List<EventHandler>> = emptyMap()
    var all: ((Event) -> Unit)? = null

    operator fun get(klass: KClass<out Event>) = events[klass]

    fun set(events: Map<KClass<out Event>, List<EventHandler>>) {
        this.events = events
    }

    fun <E : Event> emit(event: E): Boolean {
        all?.invoke(event)
        var called = false
        events[event::class]
            ?.forEach { handler ->
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

    fun <E : SuspendableEvent> emit(event: E): Boolean {
        all?.invoke(event)
        val eventHandlers = events[event::class]
        if (eventHandlers == null || eventHandlers.none { it.condition(event, dispatcher) }) {
            return false
        }
        launch {
            for (handler in eventHandlers) {
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

    fun <E : SuspendableEvent> contains(event: E): Boolean {
        val eventHandlers = events[event::class]
        return eventHandlers != null && eventHandlers.any { it.condition(event, dispatcher) }
    }

    companion object {
        private val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                throwable.printStackTrace()
            }
        }
    }
}
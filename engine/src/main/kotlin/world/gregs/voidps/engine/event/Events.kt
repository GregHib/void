package world.gregs.voidps.engine.event

import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.suspend.arriveDelay
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class Events(
    private val entity: Entity,
) : CoroutineScope {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            throwable.printStackTrace()
        }
    }
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
                if (handler.condition(event, entity)) {
                    called = true
                    runBlocking {
                        handler.block(event, entity)
                    }
                }
            }
        return called
    }

    fun <E : SuspendableEvent> emit(event: E): Boolean {
        all?.invoke(event)
        val eventHandlers = events[event::class]
        if (eventHandlers == null || eventHandlers.none { it.condition(event, entity) }) {
            return false
        }
        launch {
            if (event is Interaction) {
                event.arriveDelay()
            }
            for (handler in eventHandlers) {
                if (event is CancellableEvent && event.cancelled) {
                    return@launch
                }
                if (handler.condition(event, entity)) {
                    handler.block(event, entity)
                }
            }
        }
        return true
    }

    fun <E : SuspendableEvent> contains(event: E): Boolean {
        val eventHandlers = events[event::class]
        return eventHandlers != null && eventHandlers.any { it.condition(event, entity) }
    }
}
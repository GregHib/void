package rs.dusk.engine.event

import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import rs.dusk.utility.get
import kotlin.reflect.KClass

@Suppress("USELESS_CAST")
val eventBusModule = module {
    single { EventBus() }
}

/**
 * Handles the publication of [Event]s; [emit] to subscribers; [EventHandler].
 * Note: [EventHandler]'s are stored in a highest-first prioritised chain
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
@Suppress("UNCHECKED_CAST")
class EventBus {

    private val handlers = mutableMapOf<KClass<*>, EventHandler<*>>()

    /**
     * Attaches [handler] to the handler chain
     */
    fun <T : Event> add(clazz: KClass<T>?, handler: EventHandler<T>) {
        checkNotNull(clazz) { "Event must have a companion object." }
        var last = get(clazz)
        var next: EventHandler<T>?

        while (last != null) {
            next = last.next

            if (next == null) {
                // Append
                last.next = handler
                break
            }

            if (next.priority <= handler.priority) {
                // Insert
                last.next = handler
                handler.next = next
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
    fun <T : Event> get(clazz: KClass<T>): EventHandler<T>? {
        return handlers[clazz] as? EventHandler<T>
    }

    /**
     * Event's are only emitted to handlers which are applicable according to [EventHandler.applies]
     * An event which fails [EventHandler.checked] for any applicable handler is not emitted.
     * An event can be [Event.cancelled] by any [EventHandler] preventing further handlers from receiving the event.
     */
    fun <T : Event> emit(event: T, clazz: KClass<T>) = runBlocking {
        var handler = get(clazz)

        // Pre-check
        while (handler != null) {
            if(handler.applies(event) && !handler.checked(event)) {
                return@runBlocking
            }
            handler = handler.next
        }

        // Emit
        handler = get(clazz)
        while (handler != null) {
            if (event.cancelled) {
                break
            }

            if(handler.applies(event)) {
                handler.invoke(event)
            }

            handler = handler.next
        }
    }

    /**
     * Helper function for emitting events
     */
    inline fun <reified T : Event> emit(event: T) = emit(event, T::class)
}

/**
 * Registers a simple event handler without filter or priority
 */
inline infix fun <reified T : Event, C : EventCompanion<T>> C.then(noinline action: T.(T) -> Unit) = runBlocking {
    val handler = EventHandler<T>()
    handler.action = action
    register(T::class, handler)
}

/**
 * Registers an event handler using a [EventHandlerBuilder]
 */
inline infix fun <reified T : Event> EventHandlerBuilder<T>.then(noinline action: T.(T) -> Unit) = runBlocking {
    register(T::class, build(action))
}

/**
 * Registers [handler] with the current [EventBus]
 */
fun <T : Event> register(clazz: KClass<T>, handler: EventHandler<T>) {
    val bus: EventBus = get()
    bus.add(clazz, handler)
}
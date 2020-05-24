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
 *
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
     * Emit's [event] to all applicable handlers so long as [event] is not [Event.cancelled]
     */
    fun <T : Event> emit(event: T, clazz: KClass<T>) = runBlocking {
        var handler = get(clazz)
        while (handler != null) {
            if (event.cancelled) {
                break
            }

            handler.action.invoke(event)

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
    setAction(handler, action, null)
    register(T::class, handler)
}

/**
 * Registers an event handler using a [EventHandlerBuilder]
 */
inline infix fun <reified T : Event> EventHandlerBuilder<T>.then(noinline action: T.(T) -> Unit) = runBlocking {
    val handler = EventHandler<T>()
    setAction(handler, action, filter)
    handler.priority = priority
    register(T::class, handler)
}

/**
 * Registers [handler] with the current [EventBus]
 */
fun <T : Event> register(clazz: KClass<T>, handler: EventHandler<T>) {
    val bus: EventBus = get()
    bus.add(clazz, handler)
}

/**
 * Sets [handler]'s [action] with optional [filter]
 */
fun <T : Event> setAction(handler: EventHandler<T>, action: T.(T) -> Unit, filter: (T.() -> Boolean)?) {
    handler.action = { event ->
        if (!event.cancelled && filter?.invoke(event) != false) {
            action.invoke(event, event)
        }
    }
}
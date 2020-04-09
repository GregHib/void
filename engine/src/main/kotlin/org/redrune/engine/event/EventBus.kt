package org.redrune.engine.event

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.redrune.utility.get
import kotlin.reflect.KClass

/**
 *
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
abstract class EventBus {
    /**
     * Attaches [handler] to the handler chain
     */
    abstract fun <T : Event> add(clazz: KClass<T>?, handler: EventHandler<T>)

    /**
     * Returns [EventHandler] with matching [clazz]
     */
    abstract fun <T : Event> get(clazz: KClass<T>): EventHandler<T>?

    /**
     * Emit's [event] to all applicable handlers so long as [event] is not [Event.cancelled]
     */
    abstract fun <T : Event> emit(event: T, clazz: KClass<T>)

    /**
     * Helper function for emitting events
     */
    inline fun <reified T : Event> emit(event: T) = emit(event, T::class)
}

/**
 * Registers a simple event handler without filter or priority
 */
inline infix fun <reified T : Event, reified C : EventCompanion<T>> C.then(noinline action: T.(T) -> Unit) = runBlocking {
    val handler = EventHandler<T>()
    setActor(handler, action, null)
    register(T::class, handler)
}

/**
 * Registers an event handler using a [EventHandlerBuilder]
 */
inline infix fun <reified T : Event> EventHandlerBuilder<T>.then(noinline action: T.(T) -> Unit) = runBlocking {
    val handler = EventHandler<T>()
    setActor(handler, action, filter)
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
fun <T : Event> setActor(handler: EventHandler<T>, action: T.(T) -> Unit, filter: (T.() -> Boolean)?) {
    GlobalScope.launch {
        handler.actor = actor {
            for (event in channel) {
                if (!event.cancelled && filter?.invoke(event) != false) {
                    action.invoke(event, event)
                }
            }
        }
    }
}
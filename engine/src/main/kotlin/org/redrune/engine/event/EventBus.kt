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
     * Attaches [handler] to the start of the handler chain
     */
    abstract fun <T : Event> addFirst(clazz: KClass<T>?, handler: EventHandler<T>)

    /**
     * Attaches [handler] to the end of the handler chain
     */
    abstract fun <T : Event> addLast(clazz: KClass<T>?, handler: EventHandler<T>)

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
 * Creates an event handler
 */
inline infix fun <reified T : Event, reified C : EventCompanion<T>> C.then(noinline action: T.(T) -> Unit) = runBlocking {
    val handler = EventHandler<T>()
    setActor(handler, action, null)
    register(C::class, handler)
}

/**
 * Create an [EventHandler] with a filter
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.where(noinline filter: T.() -> Boolean) = C::class to filter

/**
 * Creates an event handler with a filter
 */
inline infix fun <reified T : Event, reified C : EventCompanion<T>> Pair<KClass<C>, T.() -> Boolean>.then(noinline action: T.(T) -> Unit) = runBlocking {
    val handler = EventHandler<T>()
    setActor(handler, action, second)
    register(first, handler)
}

/**
 * Registers [handler] with the current [EventBus]
 */
fun <C : EventCompanion<T>, T : Event> register(clazz: KClass<C>, handler: EventHandler<T>) {
    val bus: EventBus = get()
    bus.addLast(clazz.java.declaringClass.kotlin as? KClass<T>, handler)
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
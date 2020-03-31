package org.redrune.engine.event

import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
data class EventHandlerBuilder<T : Event, C : EventCompanion<T>>(var type: KClass<C>, var filter: (T.() -> Boolean)? = null, var priority: Int = 0)

/**
 * Create an [EventHandler] with priority
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.priority(priority: Int) = EventHandlerBuilder(type = C::class, priority = priority)

/**
 * Create an [EventHandler] with a filter
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.where(noinline filter: T.() -> Boolean) = EventHandlerBuilder(C::class, filter = filter)

/**
 * Append [EventHandler] with a filter
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> EventHandlerBuilder<T, C>.where(noinline filter: T.() -> Boolean) = this.apply { this.filter = filter }
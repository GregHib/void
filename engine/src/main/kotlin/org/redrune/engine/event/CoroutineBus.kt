package org.redrune.engine.event

import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
val eventBusModule = module {
    single { CoroutineBus() as EventBus }
}

@Suppress("UNCHECKED_CAST")
class CoroutineBus : EventBus() {

    private val handlers = mutableMapOf<KClass<*>, EventHandler<*>>()

    override fun <T : Event> addFirst(clazz: KClass<T>?, handler: EventHandler<T>) {
        checkNotNull(clazz) { "Event must have a companion object." }
        val current = get(clazz)
        handlers[clazz] = handler
        handler.next = current
    }

    override fun <T : Event> addLast(clazz: KClass<T>?, handler: EventHandler<T>) {
        checkNotNull(clazz) { "Event must have a companion object." }
        var last = get(clazz)
        while (last != null) {
            if (last.next == null) {
                break
            }
            last = last.next
        }

        if (last == null) {
            handlers[clazz] = handler
        } else {
            last.next = handler
        }
    }

    override fun <T : Event> get(clazz: KClass<T>): EventHandler<T>? {
        return handlers[clazz] as? EventHandler<T>
    }

    override fun <T : Event> emit(event: T, clazz: KClass<T>) = runBlocking {
        var handler = get(clazz)
        while (handler != null) {
            if (event.cancelled) {
                break
            }

            handler.actor.send(event)

            handler = handler.next
        }
    }
}
package rs.dusk.engine.event

import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
@Suppress("USELESS_CAST")
val eventBusModule = module {
    single { CoroutineBus() as EventBus }
}

@Suppress("UNCHECKED_CAST")
class CoroutineBus : EventBus() {

    private val handlers = mutableMapOf<KClass<*>, EventHandler<*>>()

    override fun <T : Event> add(clazz: KClass<T>?, handler: EventHandler<T>) {
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

    override fun clear() {
        handlers.clear()
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
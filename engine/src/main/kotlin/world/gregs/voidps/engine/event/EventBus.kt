package world.gregs.voidps.engine.event

import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import world.gregs.voidps.utility.get
import kotlin.reflect.KClass

val eventModule = module {
    single { EventBus() }
}

/**
 * Handles the publication of [Event]s; [emit] to subscribers; [EventHandler].
 * Note: [EventHandler]'s are stored in a highest-first prioritised chain
 * @author GregHib <greg@gregs.world>
 * @since March 26, 2020
 */
@Suppress("UNCHECKED_CAST")
class EventBus {

    private val handlers = mutableMapOf<KClass<*>, EventHandler<*, *>>()

    /**
     * Attaches [handler] to the handler chain
     */
    fun <T : Any, E : Event<T>> add(clazz: KClass<E>?, handler: EventHandler<T, E>) {
        checkNotNull(clazz) { "Event must have a companion object." }
        var last = get(clazz)
        var next: EventHandler<T, E>?

        while (last != null) {
            next = last.next

            if(handler.priority > last.priority) {
                handler.next = last
                handlers[clazz] = handler
                break
            }

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
    fun <T : Any, E : Event<T>> get(clazz: KClass<E>): EventHandler<T, E>? {
        return handlers[clazz] as? EventHandler<T, E>
    }

    /**
     * Event's are only emitted to handlers which are applicable according to [EventHandler.applies]
     * An event can be [Event.cancelled] by any [EventHandler] preventing further handlers from receiving the event.
     */
    fun <T : Any, E : Event<T>> emit(event: E, clazz: KClass<E>) {
        if (!checkPassed(event, clazz)) {
            return
        }

        var handler = get(clazz)
        while (handler != null) {
            if (event.cancelled) {
                break
            }

            if (handler.applies(event)) {
                try {
                    handler.invoke(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            handler = handler.next
        }
    }

    /**
     * An event must have at least one successful [EventHandler.checked] for any applicable handler to be emitted.
     */
    private fun <T : Any, E : Event<T>> checkPassed(event: E, clazz: KClass<E>): Boolean {
        var handler = get(clazz)

        while (handler != null) {
            if (handler.applies(event)) {
                return true
            }
            handler = handler.next
        }
        return false
    }

    /**
     * Helper function for emitting events
     */
    inline fun <reified T : Any, reified E : Event<T>> emit(event: E) = emit(event, E::class)
}

/**
 * Registers a simple event handler without filter or priority
 */
inline infix fun <reified T : Any, reified E : Event<T>, C : EventCompanion<E>> C.then(noinline action: E.(E) -> Unit) =
    runBlocking {
        val handler = EventHandler<T, E>()
        handler.action = action
        register(E::class, handler)
    }

/**
 * Registers an event handler using a [EventHandlerBuilder]
 */
inline infix fun <reified T : Any, reified E : Event<T>> EventHandlerBuilder<T, E>.then(noinline action: E.(E) -> Unit) =
    runBlocking {
        register(E::class, build(action))
    }

/**
 * Registers [handler] with the current [EventBus]
 */
fun <T : Any, E : Event<T>> register(clazz: KClass<E>, handler: EventHandler<T, E>) {
    val bus: EventBus = get()
    bus.add(clazz, handler)
}
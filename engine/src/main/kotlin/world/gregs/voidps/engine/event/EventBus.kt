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

    private val handlers = mutableMapOf<KClass<*>, EventHandler<*>>()

    /**
     * Attaches [handler] to the handler chain
     */
    fun <E : Event> add(clazz: KClass<E>?, handler: EventHandler<E>) {
        checkNotNull(clazz) { "Event must have a companion object." }
        var last = get(clazz)
        var next: EventHandler<E>?

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
    fun <E : Event> get(clazz: KClass<E>): EventHandler<E>? {
        return handlers[clazz] as? EventHandler<E>
    }

    /**
     * Event's are only emitted to handlers which are applicable according to [EventHandler.applies]
     */
    fun <E : Event> emit(event: E, clazz: KClass<E>) {
        if (!checkPassed(event, clazz)) {
            return
        }

        var handler = get(clazz)
        while (handler != null) {

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
    private fun <E : Event> checkPassed(event: E, clazz: KClass<E>): Boolean {
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
    inline fun <reified E : Event> emit(event: E) = emit(event, E::class)
}

/**
 * Registers a simple event handler without filter or priority
 */
inline infix fun < reified E : Event, C : EventCompanion<E>> C.then(noinline action: E.(E) -> Unit) =
    runBlocking {
        val handler = EventHandler<E>()
        handler.action = action
        register(E::class, handler)
    }

/**
 * Registers an event handler using a [EventHandlerBuilder]
 */
inline infix fun <reified E : Event> EventHandlerBuilder<E>.then(noinline action: E.(E) -> Unit) =
    runBlocking {
        register(E::class, build(action))
    }

/**
 * Registers [handler] with the current [EventBus]
 */
fun <E : Event> register(clazz: KClass<E>, handler: EventHandler<E>) {
    val bus: EventBus = get()
    bus.add(clazz, handler)
}
package world.gregs.voidps.engine.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.event.suspend.EventSuspension
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class Events(
    private val entity: Entity,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Contexts.Game
    private lateinit var events: Map<KClass<out Event>, List<EventHandler>>
    var all: ((Event) -> Unit)? = null
    var suspend: EventSuspension? = null

    operator fun get(klass: KClass<out Event>) = events[klass]

    fun set(events: Map<KClass<out Event>, List<EventHandler>>) {
        this.events = events
    }

    fun tick() {
        val suspend = suspend
        if (suspend != null) {
            if (suspend.ready()) {
                suspend.resume()
            }
            if (suspend.finished()) {
                this.suspend = null
            }
        }
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
        event.events = this
        all?.invoke(event)
        val handler = events[event::class]?.firstOrNull { it.condition(event, entity) } ?: return false
        launch {
            handler.block(event, entity)
        }
        return true
    }
}
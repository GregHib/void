package world.gregs.voidps.engine.event

interface EventDispatcher {
    fun <E: Event> emit(event: E): Boolean {
        return EventStore.events.emit(this, event)
    }

    fun <E: SuspendableEvent> emit(event: E): Boolean {
        return EventStore.events.emit(this, event)
    }
}
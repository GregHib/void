package world.gregs.voidps.engine.event

abstract class CancellableEvent : Event {
    var cancelled = false

    fun cancel() {
        cancelled = true
    }
}

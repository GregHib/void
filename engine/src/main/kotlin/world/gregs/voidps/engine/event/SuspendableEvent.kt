package world.gregs.voidps.engine.event

abstract class SuspendableEvent : Event {
    lateinit var events: Events
}
package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.event.suspend.EventSuspension

abstract class SuspendableEvent : Event {
    var suspend: EventSuspension? = null
        set(value) {
            if (value != null) suspended = true
            field = value
        }

    var suspended = false
        private set
}
package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Event

data class TimerStart(val timer: String, val restart: Boolean = false) : Event {
    var interval: Int = -1
}
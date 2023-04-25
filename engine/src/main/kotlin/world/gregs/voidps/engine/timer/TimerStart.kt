package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.CancellableEvent

data class TimerStart(val timer: String, val restart: Boolean = false) : CancellableEvent() {
    var interval: Int = -1
}
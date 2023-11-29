package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.CancellableEvent

data class TimerTick(val timer: String) : CancellableEvent() {
    var nextInterval: Int = -1
}
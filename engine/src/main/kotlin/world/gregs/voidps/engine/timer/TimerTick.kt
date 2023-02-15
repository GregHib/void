package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Event

data class TimerTick(val timer: String, val tick: Long) : Event
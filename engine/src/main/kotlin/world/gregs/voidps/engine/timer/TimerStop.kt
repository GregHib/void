package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Event

data class TimerStop(val timer: String, val logout: Boolean) : Event
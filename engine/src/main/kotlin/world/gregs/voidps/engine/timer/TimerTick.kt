package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Event

data class TimerTick(val name: String, val tick: Long) : Event
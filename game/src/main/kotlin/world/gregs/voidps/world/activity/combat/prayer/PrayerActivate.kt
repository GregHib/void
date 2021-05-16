package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.event.Event

data class PrayerActivate(val prayer: String, val curses: Boolean) : Event
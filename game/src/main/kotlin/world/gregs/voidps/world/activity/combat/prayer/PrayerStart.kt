package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.event.Event

data class PrayerStart(val prayer: String, val restart: Boolean = false) : Event
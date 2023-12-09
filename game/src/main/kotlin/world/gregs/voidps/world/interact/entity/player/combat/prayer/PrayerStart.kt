package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.event.Event

data class PrayerStart(val prayer: String, val restart: Boolean = false) : Event
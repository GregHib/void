package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class PrayerStart(val prayer: String, val restart: Boolean = false) : Event {
    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "prayer_start"
        1 -> prayer
        else -> null
    }
}

fun prayerStart(vararg prayers: String = arrayOf("*"), override: Boolean = true, block: suspend PrayerStart.(Player) -> Unit) {
    for (prayer in prayers) {
        Events.handle("prayer_start", prayer, override = override, handler = block)
    }
}
package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class PrayerStop(val prayer: String) : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "prayer_stop"
        1 -> prayer
        else -> null
    }
}

fun prayerStop(vararg prayers: String = arrayOf("*"), override: Boolean = true, block: suspend PrayerStop.(Player) -> Unit) {
    for (prayer in prayers) {
        Events.handle("prayer_stop", prayer, override = override, handler = block)
    }
}
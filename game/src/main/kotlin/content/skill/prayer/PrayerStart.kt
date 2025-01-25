package content.skill.prayer

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class PrayerStart(val prayer: String, val restart: Boolean = false) : Event {

    override val notification: Boolean = true

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "prayer_start"
        1 -> prayer
        else -> null
    }
}

fun prayerStart(vararg prayers: String = arrayOf("*"), handler: suspend PrayerStart.(Player) -> Unit) {
    for (prayer in prayers) {
        Events.handle("prayer_start", prayer, handler = handler)
    }
}
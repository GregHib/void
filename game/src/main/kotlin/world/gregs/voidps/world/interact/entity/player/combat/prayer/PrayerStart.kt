package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class PrayerStart(val prayer: String, val restart: Boolean = false) : Event

fun prayerStart(prayer: String = "*", block: suspend PrayerStart.(Player) -> Unit) {
    on<PrayerStart>({ wildcardEquals(prayer, this.prayer) }) { character ->
        block.invoke(this, character)
    }
}

fun prayerStart(vararg prayers: String, block: suspend PrayerStart.(Player) -> Unit) {
    for (prayer in prayers) {
        on<PrayerStart>({ wildcardEquals(prayer, this.prayer) }) { character ->
            block.invoke(this, character)
        }
    }
}
package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class PrayerStart(val prayer: String, val restart: Boolean = false) : Event

fun prayerStart(filter: PrayerStart.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend PrayerStart.(Player) -> Unit) {
    on<PrayerStart>(filter, priority, block)
}

fun prayerStart(prayer: String, block: suspend PrayerStart.(Player) -> Unit) {
    on<PrayerStart>({ wildcardEquals(prayer, this.prayer) }) { character: Player ->
        block.invoke(this, character)
    }
}
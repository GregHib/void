package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class PrayerStop(val prayer: String) : Event

fun prayerStop(prayer: String = "*", block: suspend PrayerStop.(Player) -> Unit) {
    on<PrayerStop>({ wildcardEquals(prayer, this.prayer) }) { character: Player ->
        block.invoke(this, character)
    }
}
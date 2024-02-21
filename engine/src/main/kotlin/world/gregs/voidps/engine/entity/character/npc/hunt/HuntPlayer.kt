package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class HuntPlayer(
    val mode: String,
    val target: Player
) : Event

fun huntPlayer(npc: String = "*", mode: String = "*", block: suspend HuntPlayer.(npc: NPC) -> Unit) {
    on<HuntPlayer>({ wildcardEquals(npc, it.id) && wildcardEquals(mode, this.mode) }, block = block)
}

fun huntPlayer(vararg modes: String, block: suspend HuntPlayer.(npc: NPC) -> Unit) {
    for (mode in modes) {
        on<HuntPlayer>({ wildcardEquals(mode, this.mode) }, block = block)
    }
}
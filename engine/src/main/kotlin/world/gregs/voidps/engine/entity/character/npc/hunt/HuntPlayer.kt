package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

data class HuntPlayer(
    val mode: String,
    val target: Player
) : Event
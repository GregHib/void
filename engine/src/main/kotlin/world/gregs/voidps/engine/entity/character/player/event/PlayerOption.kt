package world.gregs.voidps.engine.entity.character.player.event

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player

data class PlayerOption(
    override val player: Player,
    val target: Player,
    val option: String
) : Interaction()
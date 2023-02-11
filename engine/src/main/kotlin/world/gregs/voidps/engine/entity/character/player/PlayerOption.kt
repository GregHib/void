package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction

data class PlayerOption(
    override val player: Player,
    val target: Player,
    val option: String
) : Interaction()
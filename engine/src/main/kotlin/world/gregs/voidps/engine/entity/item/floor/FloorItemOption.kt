package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player

data class FloorItemOption(
    override val player: Player,
    val item: FloorItem,
    val option: String
) : Interaction()
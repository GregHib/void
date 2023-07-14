package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.mode.interact.PlayerInteraction
import world.gregs.voidps.engine.entity.character.player.Player

data class FloorItemOption(
    override val player: Player,
    val item: FloorItem,
    val option: String
) : PlayerInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}
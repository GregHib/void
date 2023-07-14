package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.PlayerInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class ItemOnPlayer(
    override val player: Player,
    val target: Player,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : PlayerInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}
package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.mode.interact.PlayerInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class InventoryOption(
    override val player: Player,
    val inventory: String,
    val item: Item,
    val slot: Int,
    val option: String
) : PlayerInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}
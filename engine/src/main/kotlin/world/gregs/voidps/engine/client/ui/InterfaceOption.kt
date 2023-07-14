package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.mode.interact.PlayerInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class InterfaceOption(
    override val player: Player,
    val id: String,
    val component: String,
    val optionIndex: Int,
    val option: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : PlayerInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}
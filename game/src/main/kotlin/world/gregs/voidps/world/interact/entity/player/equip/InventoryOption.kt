package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.item.Item

data class InventoryOption(
    override val character: Character,
    val inventory: String,
    val item: Item,
    val slot: Int,
    val option: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}
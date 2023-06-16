package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

data class ItemOnObject(
    override val player: Player,
    override val obj: GameObject,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val container: String
) : ObjectInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}
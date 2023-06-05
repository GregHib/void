package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameMapObject

data class InterfaceOnObject(
    override val player: Player,
    override val obj: GameMapObject,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val container: String
) : ObjectInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}
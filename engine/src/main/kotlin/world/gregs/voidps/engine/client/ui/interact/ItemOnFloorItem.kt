package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.suspend.arriveDelay

data class ItemOnFloorItem(
    override val character: Character,
    val floorItem: FloorItem,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun itemOnFloorItemApproach(item: String, floorItem: String, block: suspend ItemOnFloorItem.() -> Unit) {
    on<ItemOnFloorItem>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(floorItem, this.floorItem.id) }) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnFloorItemOperate(item: String, floorItem: String, arrive: Boolean = true, block: suspend ItemOnFloorItem.() -> Unit) {
    on<ItemOnFloorItem>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(floorItem, this.floorItem.id) }) { _: Player ->
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
}
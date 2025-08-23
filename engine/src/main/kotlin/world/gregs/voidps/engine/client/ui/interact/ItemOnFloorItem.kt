package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class ItemOnFloorItem(
    override val character: Player,
    val floorItem: FloorItem,
    val item: Item,
    val itemSlot: Int,
    val inventory: String,
) : Interaction<Player>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_approach_floor_item" else "item_operate_floor_item"
        1 -> item.id
        2 -> floorItem.id
        else -> null
    }
}

fun itemOnFloorItemOperate(item: String = "*", floorItem: String = "*", arrive: Boolean = true, handler: suspend ItemOnFloorItem.() -> Unit) {
    Events.handle<ItemOnFloorItem>("item_approach_floor_item", item, floorItem) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun itemOnFloorItemApproach(item: String = "*", floorItem: String = "*", handler: suspend ItemOnFloorItem.() -> Unit) {
    Events.handle<ItemOnFloorItem>("item_approach_floor_item", item, floorItem) {
        handler.invoke(this)
    }
}

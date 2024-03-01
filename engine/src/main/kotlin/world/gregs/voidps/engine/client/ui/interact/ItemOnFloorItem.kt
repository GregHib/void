package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
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

    override fun size() = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_approach_floor_item" else "item_operate_floor_item"
        1 -> item.id
        2 -> floorItem.id
        3 -> id
        4 -> component
        else -> ""
    }
}

fun itemOnFloorItemOperate(item: String = "*", floorItem: String = "*", id: String = "*", component: String = "*", arrive: Boolean = true, block: suspend ItemOnFloorItem.() -> Unit) {
    Events.handle<ItemOnFloorItem>("item_approach_floor_item", item, floorItem, id, component) {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
}

fun itemOnFloorItemApproach(item: String = "*", floorItem: String = "*", id: String = "*", component: String = "*", block: suspend ItemOnFloorItem.() -> Unit) {
    Events.handle<ItemOnFloorItem>("item_approach_floor_item", item, floorItem, id, component) {
        block.invoke(this)
    }
}

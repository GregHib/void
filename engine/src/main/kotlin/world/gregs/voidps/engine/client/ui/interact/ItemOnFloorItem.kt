package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

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

fun itemOnFloorItemApproach(filter: ItemOnFloorItem.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnFloorItem.(Player) -> Unit) {
    on<ItemOnFloorItem>({ approach && filter(this, it) }, priority, block)
}

fun itemOnFloorItemOperate(filter: ItemOnFloorItem.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnFloorItem.(Player) -> Unit) {
    on<ItemOnFloorItem>({ operate && filter(this, it) }, priority, block)
}

fun itemOnFloorItemApproach(item: String, floorItem: String, inventory: String = "inventory", block: suspend ItemOnFloorItem.() -> Unit) {
    on<ItemOnFloorItem>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(floorItem, this.floorItem.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnFloorItemOperate(item: String, floorItem: String, inventory: String = "inventory", block: suspend ItemOnFloorItem.() -> Unit) {
    on<ItemOnFloorItem>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(floorItem, this.floorItem.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}
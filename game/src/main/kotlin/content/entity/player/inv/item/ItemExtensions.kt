package content.entity.player.inv.item

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

val Item.tradeable: Boolean
    get() = def["tradeable", true]

fun Player.addOrDrop(id: String, amount: Int = 1, inventory: Inventory = this.inventory, revealTicks: Int = 100, disappearTicks: Int = 200) {
    if (!inventory.add(id, amount)) {
        FloorItems.add(tile, id, amount, revealTicks = revealTicks, disappearTicks = disappearTicks, owner = this)
    }
}

package content.entity.player.inv.item

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

val Item.tradeable: Boolean
    get() = def["tradeable", true]

fun Player.addOrDrop(id: String, amount: Int = 1, inventory: Inventory = this.inventory) {
    if (!inventory.add(id, amount)) {
        get<FloorItems>().add(tile, id, amount, revealTicks = 100, disappearTicks = 200, owner = this)
    }
}

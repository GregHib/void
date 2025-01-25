package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory

fun Player.shopInventory(sample: Boolean = hasShopSample()): Inventory {
    val shop: String = this["shop", ""]
    val name = if (sample) "${shop}_sample" else shop
    return if (name.endsWith("general_store")) {
        GeneralStores.bind(this, name)
    } else {
        inventories.inventory(name)
    }
}
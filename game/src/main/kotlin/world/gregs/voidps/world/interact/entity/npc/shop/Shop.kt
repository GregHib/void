package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.hasShopSample(): Boolean = this["info_sample", false]

fun Player.shop(): String = this["shop"]

fun Player.shopInventory(sample: Boolean = hasShopSample()): Inventory {
    val shop: String = this["shop"]
    val name = if (sample) "${shop}_sample" else shop
    return if (name.endsWith("general_store")) {
        GeneralStores.bind(this, name)
    } else {
        inventories.inventory(name)
    }
}
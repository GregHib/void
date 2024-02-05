package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption

interfaceOption("shop_side", "inventory", "Examine") {
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}

interfaceOption("shop", "sample", "Examine") {
    val item = player.shopInventory(true)[itemSlot / 4]
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}

interfaceOption("shop", "stock", "Examine") {
    val item = player.shopInventory(false)[itemSlot / 6]
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}
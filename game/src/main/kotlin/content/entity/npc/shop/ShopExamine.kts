package content.entity.npc.shop

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption

interfaceOption("Examine", "inventory", "shop_side") {
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}

interfaceOption("Examine", "sample", "shop") {
    val item = player.shopInventory(true)[itemSlot / 4]
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}

interfaceOption("Examine", "stock", "shop") {
    val item = player.shopInventory(false)[itemSlot / 6]
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}
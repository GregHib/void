package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player

interfaceOption({ id == "shop_side" && component == "inventory" && option == "Examine" }) { player: Player ->
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}

interfaceOption({ id == "shop" && component == "sample" && option == "Examine" }) { player: Player ->
    val item = player.shopInventory(true)[itemSlot / 4]
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}

interfaceOption({ id == "shop" && component == "stock" && option == "Examine" }) { player: Player ->
    val item = player.shopInventory(false)[itemSlot / 6]
    val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
    player.message(examine)
}
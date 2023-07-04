package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "shop_side" && component == "container" && option == "Examine" }) { player: Player ->
    val examine: String = item.def.getOrNull("examine") ?: return@on
    player.message(examine)
}

on<InterfaceOption>({ id == "shop" && component == "sample" && option == "Examine" }) { player: Player ->
    val item = player.shopContainer(true)[itemSlot / 4]
    val examine: String = item.def.getOrNull("examine") ?: return@on
    player.message(examine)
}

on<InterfaceOption>({ id == "shop" && component == "stock" && option == "Examine" }) { player: Player ->
    val item = player.shopContainer(false)[itemSlot / 6]
    val examine: String = item.def.getOrNull("examine") ?: return@on
    player.message(examine)
}
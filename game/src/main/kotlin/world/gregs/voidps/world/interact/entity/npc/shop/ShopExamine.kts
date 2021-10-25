import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer

on<InterfaceOption>({ id == "shop_side" && component == "container" && option == "Examine" }) { player: Player ->
    val examine = item.def.getOrNull("examine") as? String ?: return@on
    player.message(examine)
}

on<InterfaceOption>({ id == "shop" && component == "sample" && option == "Examine" }) { player: Player ->
    val item = player.shopContainer(true).getItem(itemIndex / 4)
    val examine = item.def.getOrNull("examine") as? String ?: return@on
    player.message(examine)
}

on<InterfaceOption>({ id == "shop" && component == "stock" && option == "Examine" }) { player: Player ->
    val item = player.shopContainer(false).getItem(itemIndex / 6)
    val examine = item.def.getOrNull("examine") as? String ?: return@on
    player.message(examine)
}
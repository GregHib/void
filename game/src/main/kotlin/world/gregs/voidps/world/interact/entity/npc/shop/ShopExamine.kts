import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer

val itemDefs: ItemDefinitions by inject()

on<InterfaceOption>({ name == "shop_side" && component == "container" && option == "Examine" }) { player: Player ->
    val examine = itemDefs.get(item).getOrNull("examine") as? String ?: return@on
    player.message(examine)
}

on<InterfaceOption>({ name == "shop" && component == "sample" && option == "Examine" }) { player: Player ->
    val item = player.shopContainer(true).getItemId(itemIndex / 4)
    val examine = itemDefs.get(item).getOrNull("examine") as? String ?: return@on
    player.message(examine)
}

on<InterfaceOption>({ name == "shop" && component == "stock" && option == "Examine" }) { player: Player ->
    val item = player.shopContainer(false).getItemId(itemIndex / 6)
    val examine = itemDefs.get(item).getOrNull("examine") as? String ?: return@on
    player.message(examine)
}
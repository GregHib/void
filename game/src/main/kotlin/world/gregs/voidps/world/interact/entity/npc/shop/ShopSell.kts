import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.contain.ContainerResult
import world.gregs.voidps.engine.entity.character.contain.give
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.world.interact.entity.npc.shop.shop
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer
import kotlin.math.min

val itemDefs: ItemDefinitions by inject()

on<InterfaceOption>({ id == "shop_side" && component == "container" && option == "Value" }) { player: Player ->
    if (!sellable(item, player)) {
        player.message("You can't sell this item to this shop.")
        return@on
    }
    val price = getSellPrice(item.id)
    player.message("${item.def.name}: shop will buy for $price ${player["shop_currency", "coin"].plural(price)}.")
}

on<InterfaceOption>({ id == "shop_side" && component == "container" && option.startsWith("Sell") }) { player: Player ->
    val amount = when (option) {
        "Sell 1" -> 1
        "Sell 5" -> 5
        "Sell 10" -> 10
        "Sell 50" -> 50
        else -> return@on
    }
    sell(player, item, itemIndex, amount)
}

fun getSellPrice(item: String): Int {
    val def = itemDefs.get(item)
    return (def.cost * 0.4).toInt()
}

fun sell(player: Player, item: Item, index: Int, amount: Int) {
    val container = player.shopContainer(false)
    if (!sellable(item, player)) {
        player.message("You can't sell this item to this shop.")
        return
    }
    val available = player.inventory.getCount(item).toInt()
    val actualAmount = min(available, amount)
    if (actualAmount > 0 && player.inventory.move(container, item.id, actualAmount, index)) {
        val currency: String = player["shop_currency", "coins"]
        val price = getSellPrice(item.id)
        player.give(currency, price * actualAmount)
    } else {
        if (player.inventory.result == ContainerResult.Full) {
            player.message("Shop is currently full.")
        }
    }
}

fun sellable(item: Item, player: Player): Boolean {
    val container = player.shopContainer(false)
    val store = player.shop().contains("general_store")
    return (store && item.def["tradeable", true] && item.id != "coins") || container.contains(item.id)
}
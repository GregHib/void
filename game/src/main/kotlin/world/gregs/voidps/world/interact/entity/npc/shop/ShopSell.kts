import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer

on<InterfaceOption>({ id == "shop_side" && component == "container" && option == "Value" }) { player: Player ->
    val container = player.shopContainer(false)
    if (container.restricted(item.id)) {
        player.message("You can't sell this item to this shop.")
        return@on
    }
    val price = item.sellPrice()
    val currency = player.shopCurrency().plural(price)
    player.message("${item.def.name}: shop will buy for $price $currency.")
}

on<InterfaceOption>({ id == "shop_side" && component == "container" && option.startsWith("Sell") }) { player: Player ->
    val amount = when (option) {
        "Sell 1" -> 1
        "Sell 5" -> 5
        "Sell 10" -> 10
        "Sell 50" -> 50
        else -> return@on
    }
    sell(player, item, amount)
}

fun Item.sellPrice() = (def.cost * 0.4).toInt()

fun Player.shopCurrency(): String = this["shop_currency", "coins"]

fun sell(player: Player, item: Item, amount: Int) {
    player.inventory.transaction {
        val removed = removeToLimit(item.id, amount)
        val shop = link(player.shopContainer(false))
        val added = shop.addToLimit(item.id, removed)
        if (added == 0) {
            return@transaction
        }
        if (added < removed) {
            player.message("The shop is currently full.")
            add(item.id, removed - added)
        }
        val price = item.sellPrice()
        if (price > 0) {
            add(player.shopCurrency(), price * added)
        }
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        TransactionError.Invalid -> player.message("You can't sell this item to this shop.")
        else -> {}
    }
}
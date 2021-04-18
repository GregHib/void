import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.contain.*
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.npc.shop.Price
import world.gregs.voidps.world.interact.entity.npc.shop.hasShopSample
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer
import kotlin.math.min

val itemDefs: ItemDefinitions by inject()

on<InterfaceOption>({ name == "item_info" && component == "button" && option.startsWith("Buy") }) { player: Player ->
    val amount = when (option) {
        "Buy 1" -> 1
        "Buy 5" -> 5
        "Buy 10" -> 10
        "Buy 50" -> 50
        else -> return@on
    }
    val id: Int = player.getVar("info_item")
    val item = itemDefs.getName(id)

    val container = player.shopContainer()
    val index = container.indexOf(item)
    if (player.hasShopSample()) {
        take(player, container, index, amount)
    } else {
        buy(player, container, index, amount)
    }
}

on<InterfaceOption>({ name == "shop" && component == "sample" && option.startsWith("Take") }) { player: Player ->
    val amount = when (option) {
        "Take-1" -> 1
        "Take-5" -> 5
        "Take-10" -> 10
        "Take-50" -> 50
        else -> return@on
    }
    take(player, player.shopContainer(true), itemIndex / 4, amount)
}

fun take(player: Player, shop: Container, index: Int, amount: Int) {
    val amountAvailable = shop.getAmount(index)
    val actualAmount = min(amountAvailable, amount)
    if (actualAmount < amount) {
        player.message("Shop has run out of stock.")
    }
    if (amountAvailable <= 0) {
        return
    }
    shop.move(player.inventory, shop.getItem(index), actualAmount, index)
    when (shop.result) {
        ContainerResult.Full -> player.inventoryFull()
    }
}

on<InterfaceOption>({ name == "shop" && component == "stock" && option.startsWith("Buy") }) { player: Player ->
    val amount = when (option) {
        "Buy-1" -> 1
        "Buy-5" -> 5
        "Buy-10" -> 10
        "Buy-50" -> 50
        "Buy-500" -> 500
        else -> return@on
    }
    buy(player, player.shopContainer(false), itemIndex / 6, amount)
}

fun buy(player: Player, shop: Container, index: Int, amount: Int) {
    val item = shop.getItem(index)
    val def = itemDefs.get(item)
    val price = Price.getPrice(player, def.id, index, amount)

    val currency: String = player["shop_currency", "coins"]
    val currencyAvailable = player.inventory.getCount(currency).toInt()


    val amountAvailable = shop.getAmount(index)
    val budget = currencyAvailable / price

    if (amountAvailable <= 0) {
        player.message("Shop has run out of stock")
        return
    }

    if (amount > budget) {
        player.message("You don't have enough ${currency.replace("_", " ").capitalize()}.")
        return
    }

    val actualAmount = min(amountAvailable, amount)
    val cost = actualAmount * price
    if (shop.move(player.inventory, item, actualAmount, index)) {
        player.purchase(cost, currency)
        if (actualAmount < amount) {
            player.message("Shop has run out of stock.")
        }
    } else {
        when (shop.result) {
            ContainerResult.Full -> player.inventoryFull()
            ContainerResult.Deficient -> player.message("Shop has run out of stock.")
        }
    }
}
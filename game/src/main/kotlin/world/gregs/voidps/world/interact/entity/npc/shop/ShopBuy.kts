import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.moveToLimit
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.npc.shop.Price
import world.gregs.voidps.world.interact.entity.npc.shop.hasShopSample
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer

val itemDefs: ItemDefinitions by inject()
val logger = InlineLogger()

on<InterfaceOption>({ id == "item_info" && component == "button" && option.startsWith("Buy") }) { player: Player ->
    val amount = when (option) {
        "Buy 1" -> 1
        "Buy 5" -> 5
        "Buy 10" -> 10
        "Buy 50" -> 50
        else -> return@on
    }
    val id: Int = player.getVar("info_item")
    val item = itemDefs.get(id).stringId
    val container = player.shopContainer()
    val index = container.indexOf(item)
    if (player.hasShopSample()) {
        take(player, container, index, amount)
    } else {
        buy(player, container, index, amount)
    }
}

on<InterfaceOption>({ id == "shop" && component == "sample" && option.startsWith("Take") }) { player: Player ->
    val amount = when (option) {
        "Take-1" -> 1
        "Take-5" -> 5
        "Take-10" -> 10
        "Take-50" -> 50
        else -> return@on
    }
    take(player, player.shopContainer(true), itemSlot / 4, amount)
}

fun take(player: Player, shop: Container, index: Int, amount: Int) {
    val item = shop[index]
    if (item.isEmpty()) {
        logger.warn { "Error taking from shop ${shop.id} $index $amount" }
        return
    }
    shop.moveToLimit(item.id, amount, player.inventory)
    when (shop.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        is TransactionError.Deficient -> player.message("Shop has run out of stock.")
        TransactionError.Invalid -> logger.warn { "Error taking from shop ${shop.id} $item $amount" }
        else -> {}
    }
}

on<InterfaceOption>({ id == "shop" && component == "stock" && option.startsWith("Buy") }) { player: Player ->
    val amount = when (option) {
        "Buy-1" -> 1
        "Buy-5" -> 5
        "Buy-10" -> 10
        "Buy-50" -> 50
        "Buy-500" -> 500
        else -> return@on
    }
    buy(player, player.shopContainer(false), itemSlot / 6, amount)
}

fun buy(player: Player, shop: Container, index: Int, amount: Int) {
    val item = shop[index]
    if (item.amount <= 0) {
        player.message("Shop has run out of stock")
        return
    }
    val price = Price.getPrice(player, item.id, index, amount)
    val currency: String = player["shop_currency", "coins"]
    val currencyAvailable = player.inventory.getCount(currency)
    val budget = currencyAvailable / price
    if (amount > budget) {
        player.message("You don't have enough ${currency.toTitleCase()}.")
        return
    }
    player.inventory.transaction {
        val removed = link(shop).removeToLimit(item.id, amount)
        if (removed < amount) {
            player.message("Shop has run out of stock.")
        }
        remove(currency, removed * price)
        add(item.id, removed)
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        is TransactionError.Deficient -> player.message("You don't have enough ${currency.toTitleCase()}.")
        TransactionError.Invalid -> logger.warn { "Error buying from shop ${shop.id} $item ${shop.transaction.error}" }
        else -> {}
    }
}
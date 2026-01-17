package content.entity.npc.shop.buy

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.hasShopSample
import content.entity.npc.shop.shopInventory
import content.entity.npc.shop.stock.Price
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.moveToLimit
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import kotlin.math.min

class ShopBuy(val itemDefs: ItemDefinitions) : Script {
    val logger = InlineLogger()

    init {
        interfaceOption(id = "item_info:button") {
            val amount = when (it.option) {
                "Buy 1" -> 1
                "Buy 5" -> 5
                "Buy 10" -> 10
                "Buy 50" -> 50
                else -> return@interfaceOption
            }
            val id: Int = get("info_item") ?: return@interfaceOption
            val item = itemDefs.get(id).stringId
            val inventory = shopInventory()
            val index = inventory.indexOf(item)
            if (hasShopSample()) {
                take(this, inventory, index, amount)
            } else {
                buy(this, inventory, index, amount)
            }
        }

        interfaceOption(id = "shop:sample") { (_, itemSlot, option) ->
            val amount = when (option) {
                "Take-1" -> 1
                "Take-5" -> 5
                "Take-10" -> 10
                "Take-50" -> 50
                else -> return@interfaceOption
            }
            take(this, shopInventory(true), itemSlot / 4, amount)
        }

        interfaceOption(id = "shop:stock") { (_, itemSlot, option) ->
            val amount = when (option) {
                "Buy-1" -> 1
                "Buy-5" -> 5
                "Buy-10" -> 10
                "Buy-50" -> 50
                "Buy-500" -> 500
                else -> return@interfaceOption
            }
            buy(this, shopInventory(false), itemSlot / 6, amount)
        }
    }

    fun take(player: Player, shop: Inventory, index: Int, amount: Int) {
        val item = shop[index]
        if (item.isEmpty()) {
            logger.warn { "Error taking from shop ${shop.id} $index $amount" }
            return
        }
        if (item.amount <= 0) {
            player.message("Shop has run out of stock.")
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

    fun buy(player: Player, shop: Inventory, index: Int, amount: Int) {
        val item = shop[index]
        val price = Price.getPrice(player, item.id, index, amount)
        val currency: String = player["shop_currency", "coins"]
        val budget = player.inventory.count(currency) / price
        val available = shop[index].amount
        if (available <= 0) {
            player.message("The shop has run out of stock.")
            return
        } else if (budget < available && budget < amount) {
            player.message("You don't have enough $currency.")
        } else if (available < budget && available < amount) {
            player.message("The shop has run out of stock.")
        }
        val actualAmount = min(budget, min(amount, available))
        if (actualAmount == 0) {
            return
        }
        var added = 0
        player.inventory.transaction {
            added = addToLimit(item.id, actualAmount)
            if (added == 0) {
                error = TransactionError.Full()
            }
            link(shop).remove(item.id, added)
            remove(currency, added * price)
        }
        when (player.inventory.transaction.error) {
            TransactionError.None -> {
                if (added < actualAmount) player.inventoryFull()
                val actual = Item(item.id, added)
                AuditLog.event(player, "bought", actual, shop.id, price)
                Items.bought(player, actual)
            }
            is TransactionError.Full -> player.inventoryFull()
            TransactionError.Invalid -> logger.warn { "Error buying from shop ${shop.id} $item ${shop.transaction.error}" }
            else -> {}
        }
    }
}

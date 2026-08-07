package content.entity.npc.shop.buy

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.hasShopSample
import content.entity.npc.shop.shopInventory
import content.entity.npc.shop.stock.Price
import content.skill.dungeoneering.DungeonShop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.menu
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

class ShopBuy : Script {

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
            val item = ItemDefinitions.get(id).stringId
            if (menu == "dungeon_shop") {
                DungeonShop.buy(this, Item(item), amount)
                return@interfaceOption
            }
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
        buy(player, item, amount, price, index, shop)
    }

    companion object {
        private val logger = InlineLogger()

        fun buy(
            player: Player,
            item: Item,
            amount: Int,
            price: Int,
            index: Int = -1,
            shop: Inventory? = null,
            deficient: String? = null,
        ) {
            val currency = player["shop_currency", "coins"]
            val budget = player.inventory.count(currency) / price
            val available = shop?.get(index)?.amount ?: Int.MAX_VALUE
            if (available <= 0) {
                player.message("The shop has run out of stock.")
                return
            } else if (budget < available && budget < amount) {
                player.message(deficient ?: "You don't have enough $currency.")
            } else if (available < budget && available < amount) {
                player.message("The shop has run out of stock.")
            }
            val actualAmount = minOf(budget, amount, available)
            if (actualAmount == 0) {
                return
            }
            var added = 0
            player.inventory.transaction {
                added = addToLimit(item.id, actualAmount)
                if (added == 0) {
                    error = TransactionError.Full()
                }
                if (shop != null) {
                    link(shop).remove(item.id, added)
                }
                remove(currency, added * price)
            }
            val shopId = shop?.id ?: "dungeoneering_smuggler"
            when (player.inventory.transaction.error) {
                TransactionError.None -> {
                    if (added < actualAmount) player.inventoryFull()
                    val actual = Item(item.id, added)
                    AuditLog.event(player, "bought", actual, shopId, price)
                    Items.bought(player, actual)
                }
                is TransactionError.Full -> player.inventoryFull()
                TransactionError.Invalid -> logger.warn { "Error buying from shop $shopId $item ${shop?.transaction?.error}" }
                else -> {}
            }
        }
    }
}

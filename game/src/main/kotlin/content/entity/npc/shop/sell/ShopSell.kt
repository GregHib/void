package content.entity.npc.shop.sell

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.shopInventory
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit

@Script
class ShopSell {

    val logger = InlineLogger()

    init {
        interfaceOption("Value", "inventory", "shop_side") {
            val inventory = player.shopInventory(false)
            if (inventory.restricted(item.id)) {
                player.message("You can't sell this item to this shop.")
                return@interfaceOption
            }
            val price = item.sellPrice()
            val currency = player.shopCurrency().plural(price)
            player.message("${item.def.name}: shop will buy for $price $currency.")
        }

        interfaceOption("Sell *", "inventory", "shop_side") {
            val amount = when (option) {
                "Sell 1" -> 1
                "Sell 5" -> 5
                "Sell 10" -> 10
                "Sell 50" -> 50
                else -> return@interfaceOption
            }
            sell(player, item, amount)
        }
    }

    fun Item.sellPrice() = (def.cost * 0.4).toInt()

    fun Player.shopCurrency(): String = this["shop_currency", "coins"]

    fun sell(player: Player, item: Item, amount: Int) {
        val notNoted = if (item.isNote) item.noted else item
        if (notNoted == null) {
            logger.warn { "Issue selling noted item $item" }
            player.message("You can't sell this item to this shop.")
            return
        }
        val shop = player.shopInventory(false)
        var moved = 0
        val price = item.sellPrice()
        player.inventory.transaction {
            moved = moveToLimit(item.id, amount, shop, notNoted.id)
            if (moved == 0) {
                this.error = TransactionError.Full(amount)
                return@transaction
            }
            if (price > 0) {
                add(player.shopCurrency(), price * moved)
            }
        }
        when (player.inventory.transaction.error) {
            is TransactionError.Full -> {
                if (player.inventory.isFull() && !player.inventory.contains("coins")) {
                    player.inventoryFull()
                } else if (shop.isFull()) {
                    player.message("The shop is currently full.")
                } else {
                    player.message("You can't sell this item to this shop.")
                }
            }
            TransactionError.Invalid -> player.message("You can't sell this item to this shop.")
            else -> {
                val actual = Item(item.id, moved)
                AuditLog.event(player, "sold", actual, shop.id, price)
                player.emit(SoldItem(actual, shop.id))
            }
        }
    }
}

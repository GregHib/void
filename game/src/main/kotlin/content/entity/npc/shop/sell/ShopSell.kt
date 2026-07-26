package content.entity.npc.shop.sell

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.shop
import content.entity.npc.shop.shopInventory
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import kotlin.math.min

class ShopSell(val inventoryDefinitions: InventoryDefinitions) : Script {

    val logger = InlineLogger()

    init {
        interfaceOption("Value", "shop_side:inventory") { (item) ->
            val inventory = shopInventory(false)
            if (inventory.restricted(item.id)) {
                message("You can't sell this item to this shop.")
                return@interfaceOption
            }
            val unnoted = (if (item.isNote) item.noted else item) ?: item
            val price = if (sampleDeficit(unnoted.id) != null) 0 else item.sellPrice()
            val currency = shopCurrency().plural(price)
            message("${item.def.name}: shop will buy for $price $currency.")
        }

        interfaceOption(id = "shop_side:inventory") { (item, _, option) ->
            val amount = when (option) {
                "Sell 1" -> 1
                "Sell 5" -> 5
                "Sell 10" -> 10
                "Sell 50" -> 50
                else -> return@interfaceOption
            }
            sell(this, item, amount)
        }
    }

    fun Item.sellPrice() = (def.cost * 0.4).toInt()

    fun Player.shopCurrency(): String = this["shop_currency", "coins"]

    /**
     * The shop's sample inventory paired with how many of [itemId] are missing
     * from its default stock, or null if the item isn't a sample or samples are full.
     */
    fun Player.sampleDeficit(itemId: String): Pair<Inventory, Int>? {
        val definition = inventoryDefinitions.getOrNull("${shop()}_sample") ?: return null
        val index = definition.ids?.indexOf(ItemDefinitions.get(itemId).id) ?: return null
        if (index == -1) {
            return null
        }
        val maximum = definition.amounts?.getOrNull(index) ?: return null
        val sample = shopInventory(true)
        val deficit = maximum - sample[index].amount
        return if (deficit > 0) sample to deficit else null
    }

    fun sell(player: Player, item: Item, amount: Int) {
        val notNoted = if (item.isNote) item.noted else item
        if (notNoted == null) {
            logger.warn { "Issue selling noted item $item" }
            player.message("You can't sell this item to this shop.")
            return
        }
        // Samples are returned to the sample stock for free until it has respawned
        var returned = 0
        val (sampleInventory, deficit) = player.sampleDeficit(notNoted.id) ?: (null to 0)
        if (sampleInventory != null && deficit > 0) {
            player.inventory.transaction {
                returned = moveToLimit(item.id, min(amount, deficit), sampleInventory, notNoted.id)
            }
            if (player.inventory.transaction.error != TransactionError.None) {
                returned = 0
            } else if (returned > 0) {
                val actual = Item(item.id, returned)
                AuditLog.event(player, "sold", actual, sampleInventory.id, 0)
                Items.sold(player, actual)
            }
        }
        val remaining = amount - returned
        if (remaining <= 0) {
            return
        }
        val shop = player.shopInventory(false)
        var moved = 0
        val price = item.sellPrice()
        player.inventory.transaction {
            moved = moveToLimit(item.id, remaining, shop, notNoted.id)
            if (moved == 0) {
                this.error = TransactionError.Full(remaining)
                return@transaction
            }
            if (price > 0) {
                add(player.shopCurrency(), price * moved)
            }
        }
        when (player.inventory.transaction.error) {
            is TransactionError.Full -> {
                if (returned > 0) {
                    // Sold-out remainder after a sample return isn't an error
                } else if (player.inventory.isFull() && !player.inventory.contains("coins")) {
                    player.inventoryFull()
                } else if (shop.isFull()) {
                    player.message("The shop is currently full.")
                } else {
                    player.message("You can't sell this item to this shop.")
                }
            }
            TransactionError.Invalid -> if (returned == 0) {
                player.message("You can't sell this item to this shop.")
            }
            else -> {
                val actual = Item(item.id, moved)
                AuditLog.event(player, "sold", actual, shop.id, price)
                Items.sold(player, actual)
            }
        }
    }
}

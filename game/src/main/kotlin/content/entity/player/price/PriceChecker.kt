package content.entity.player.price

import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import content.entity.player.dialogue.type.intEntry
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.social.trade.offer
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.inventoryUpdate
import world.gregs.voidps.engine.inv.moveAll
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit

class PriceChecker : Script {

    init {
        interfaceOpen("price_checker") { id ->
            interfaceOptions.unlockAll(id, "items", 0 until 28)
            set("price_checker_total", 0)
            set("price_checker_limit", Int.MAX_VALUE)
            open("price_checker_side")
        }

        interfaceOption("Remove-*", "items", "price_checker") {
            val amount = when (option) {
                "Remove-1" -> 1
                "Remove-5" -> 5
                "Remove-10" -> 10
                "Remove-All" -> player.offer.count(item.id)
                "Remove-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            player.offer.transaction {
                moveToLimit(item.id, amount, player.inventory)
            }
            when (player.offer.transaction.error) {
                is TransactionError.Full -> player.inventoryFull()
                else -> {}
            }
        }

        interfaceClose("price_checker") { player ->
            player.close("price_checker_side")
            player.sendScript("clear_dialogues")
            player.offer.moveAll(player.inventory)
        }

        interfaceOpen("price_checker_side") { id ->
            tab(Tab.Inventory)
            interfaceOptions.send(id, "items")
            interfaceOptions.unlockAll(id, "items", 0 until 28)
            sendInventory(inventory)
        }

        interfaceOption("Add*", "items", "price_checker_side") {
            val amount = when (option) {
                "Add" -> 1
                "Add-5" -> 5
                "Add-10" -> 10
                "Add-All" -> player.inventory.count(item.id)
                "Add-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            player.inventory.transaction {
                moveToLimit(item.id, amount, player.offer)
            }
            when (player.inventory.transaction.error) {
                is TransactionError.Invalid -> player.message("That item is not tradeable.")
                else -> {}
            }
        }

        interfaceClose("price_checker_side") { player ->
            player.open("inventory")
        }

        inventoryUpdate("trade_offer") { player ->
            var total = 0L
            for (index in player.offer.indices) {
                val item = player.offer[index]
                if (item.isEmpty()) {
                    continue
                }
                val notNoted = if (item.isNote) item.noted ?: item else item
                val price = notNoted.def["price", notNoted.def.cost]
                player["value_$index"] = price
                total += price * item.amount
            }
            player["price_checker_total"] = total.toInt()
        }
    }

    /*
        Price checker interface
     */

    /*
        Price checker inventory interface
     */
}

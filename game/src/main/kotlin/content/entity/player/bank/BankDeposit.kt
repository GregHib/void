package content.entity.player.bank

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.Bank.tabIndex
import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shift

@Script
class BankDeposit {

    val logger = InlineLogger()

    init {
        interfaceOption("Deposit-*", "inventory", "bank_side") {
            val amount = when (option) {
                "Deposit-1" -> 1
                "Deposit-5" -> 5
                "Deposit-10" -> 10
                "Deposit-*" -> player["last_bank_amount", 0]
                "Deposit-All" -> Int.MAX_VALUE
                "Deposit-X" -> intEntry("Enter amount:").also {
                    player["last_bank_amount"] = it
                }
                else -> return@interfaceOption
            }
            deposit(player, player.inventory, item, amount)
        }

        interfaceOption("Deposit carried items", "carried", "bank") {
            if (player.inventory.isEmpty()) {
                player.message("You have no items in your inventory to deposit.")
            } else {
                bankAll(player, player.inventory)
            }
        }

        interfaceOption("Deposit carried items", "carried", "bank_deposit_box") {
            if (player.inventory.isEmpty()) {
                player.message("You have no items in your inventory to deposit.")
            } else {
                bankAll(player, player.inventory)
            }
        }

        interfaceOption("Deposit worn items", "worn", "bank") {
            if (player.equipment.isEmpty()) {
                player.message("You have no equipped items to deposit.")
            } else {
                bankAll(player, player.equipment)
            }
        }

        interfaceOption("Deposit worn items", "worn", "bank_deposit_box") {
            if (player.equipment.isEmpty()) {
                player.message("You have no equipped items to deposit.")
            } else {
                bankAll(player, player.equipment)
            }
        }

        interfaceOption("Deposit beast of burden inventory", "burden", "bank") {
            // TODO no familiar & no bob familiar messages
            if (player.beastOfBurden.isEmpty()) {
                player.message("Your familiar has no items to deposit.")
            } else {
                bankAll(player, player.beastOfBurden)
            }
        }

        interfaceOption("Deposit beast of burden inventory", "burden", "bank_deposit_box") {
            // TODO no familiar & no bob familiar messages
            if (player.beastOfBurden.isEmpty()) {
                player.message("Your familiar has no items to deposit.")
            } else {
                bankAll(player, player.beastOfBurden)
            }
        }
    }

    fun deposit(player: Player, inventory: Inventory, item: Item, amount: Int): Boolean {
        if ((player.menu != "bank" && player.menu != "bank_deposit_box") || amount < 1) {
            return true
        }

        if (item.def["unbankable", 0] == 1) {
            player.message("This item cannot be banked.")
            return true
        }

        val notNoted = if (item.isNote) item.noted else item
        if (notNoted == null) {
            logger.warn { "Issue depositing noted item $item" }
            return true
        }

        val tab = player["open_bank_tab", 1] - 1
        val bank = player.bank
        var shifted = false
        inventory.transaction {
            val existing = bank.indexOf(notNoted.id)
            val moved = moveToLimit(item.id, amount, bank, notNoted.id)
            if (moved == 0) {
                error = TransactionError.Full()
            } else if (moved > 0 && tab > 0 && existing == -1) {
                // Shift item into tab
                val index = bank.freeIndex() - 1
                val to = tabIndex(player, tab + 1)
                link(bank).shift(index, to)
                shifted = true
            }
        }
        when (inventory.transaction.error) {
            TransactionError.None -> if (shifted) player.inc("bank_tab_$tab")
            is TransactionError.Full -> player.message("Your bank is too full to deposit any more.")
            TransactionError.Invalid -> logger.info { "Bank deposit issue: $player $item $amount $inventory " }
            else -> {}
        }
        return true
    }

    fun bankAll(player: Player, inventory: Inventory) {
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isNotEmpty()) {
                deposit(player, inventory, item, item.amount)
            }
        }
    }
}

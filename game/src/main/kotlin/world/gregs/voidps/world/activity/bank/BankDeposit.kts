package world.gregs.voidps.world.activity.bank

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.beastOfBurden
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.Bank.tabIndex
import world.gregs.voidps.world.interact.dialogue.type.intEntry

val logger = InlineLogger()

on<InterfaceOption>({ id == "bank_side" && component == "inventory" && option.startsWith("Deposit") }) { player: Player ->
    val amount = when (option) {
        "Deposit-1" -> 1
        "Deposit-5" -> 5
        "Deposit-10" -> 10
        "Deposit-*" -> player["last_bank_amount", 0]
        "Deposit-All" -> Int.MAX_VALUE
        else -> return@on
    }
    deposit(player, player.inventory, item, amount)
}

on<InterfaceOption>({ id == "bank_side" && component == "inventory" && option == "Deposit-X" }) { player: Player ->
    val amount = intEntry("Enter amount:")
    player["last_bank_amount"] = amount
    deposit(player, player.inventory, item, amount)
}

fun deposit(player: Player, inventory: world.gregs.voidps.engine.contain.Inventory, item: Item, amount: Int): Boolean {
    if (player.menu != "bank" || amount < 1) {
        return true
    }

    if (!item.def["bankable", true]) {
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

on<InterfaceOption>({ id == "bank" && component == "carried" && option == "Deposit carried items" }) { player: Player ->
    if (player.inventory.isEmpty()) {
        player.message("You have no items in your inventory to deposit.")
    } else {
        bankAll(player, player.inventory)
    }
}

on<InterfaceOption>({ id == "bank" && component == "worn" && option == "Deposit worn items" }) { player: Player ->
    if (player.equipment.isEmpty()) {
        player.message("You have no equipped items to deposit.")
    } else {
        bankAll(player, player.equipment)
    }
}

on<InterfaceOption>({ id == "bank" && component == "burden" && option == "Deposit beast of burden inventory" }) { player: Player ->
    // TODO no familiar & no bob familiar messages
    if (player.beastOfBurden.isEmpty()) {
        player.message("Your familiar has no items to deposit.")
    } else {
        bankAll(player, player.beastOfBurden)
    }
}

fun bankAll(player: Player, inventory: world.gregs.voidps.engine.contain.Inventory) {
    for (index in inventory.indices) {
        val item = inventory[index]
        if (item.isNotEmpty()) {
            deposit(player, inventory, item, item.amount)
        }
    }
}
package world.gregs.voidps.world.activity.bank

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.world.interact.dialogue.type.intEntry

val logger = InlineLogger()

on<InterfaceOption>({ id == "bank" && component == "inventory" && option.startsWith("Withdraw") }) { player: Player ->
    val amount = when (option) {
        "Withdraw-1" -> 1
        "Withdraw-5" -> 5
        "Withdraw-10" -> 10
        "Withdraw-*" -> player["last_bank_amount", 0]
        "Withdraw-All" -> Int.MAX_VALUE
        "Withdraw-All but one" -> item.amount - 1
        else -> return@on
    }
    withdraw(player, item, itemSlot, amount)
}

on<InterfaceOption>({ id == "bank" && component == "inventory" && option == "Withdraw-X" }) { player: Player ->
    val amount = intEntry("Enter amount:")
    player["last_bank_amount"] = amount
    withdraw(player, item, itemSlot, amount)
}

on<InterfaceOption>({ id == "bank" && component == "note_mode" && option == "Toggle item/note withdrawl" }) { player: Player ->
    player.toggle("bank_notes")
}

fun withdraw(player: Player, item: Item, index: Int, amount: Int) {
    if (player.menu != "bank" || amount < 1) {
        return
    }

    val note = player["bank_notes", false]
    val noted = if (note) item.noted ?: item else item
    if (note && noted.id == item.id) {
        player.message("This item cannot be withdrawn as a note.")
    }
    var removed = false
    player.bank.transaction {
        val inv = player.inventory
        val moved = moveToLimit(item.id, amount, inv, noted.id)
        if (moved <= 0) {
            error = TransactionError.Full()
            return@transaction
        }
        if (inventory[index].isEmpty()) {
            shiftToFreeIndex(index)
            removed = true
        }
    }
    when (player.bank.transaction.error) {
        TransactionError.None -> if (removed) Bank.decreaseTab(player, Bank.getTab(player, index))
        is TransactionError.Full -> player.message("Your inventory is full.")
        TransactionError.Invalid -> logger.info { "Bank withdraw issue: $player $item $amount" }
        else -> {}
    }
}
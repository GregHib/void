package content.entity.player.bank

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shiftToFreeIndex
import content.entity.player.dialogue.type.intEntry

val logger = InlineLogger()

interfaceOption("Withdraw-*", "inventory", "bank") {
    val amount = when (option) {
        "Withdraw-1" -> 1
        "Withdraw-5" -> 5
        "Withdraw-10" -> 10
        "Withdraw-*" -> player["last_bank_amount", 0]
        "Withdraw-All" -> Int.MAX_VALUE
        "Withdraw-All but one" -> item.amount - 1
        "Withdraw-X" -> intEntry("Enter amount:").also {
            player["last_bank_amount"] = it
        }
        else -> return@interfaceOption
    }
    withdraw(player, item, itemSlot, amount)
}

interfaceOption("Toggle item/note withdrawl", "note_mode", "bank") {
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
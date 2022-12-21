package world.gregs.voidps.world.activity.bank

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.variable.decVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.intEntry

val logger = InlineLogger()

on<InterfaceOption>({ id == "bank" && component == "container" && option.startsWith("Withdraw") }) { player: Player ->
    val amount = when (option) {
        "Withdraw-1" -> 1
        "Withdraw-5" -> 5
        "Withdraw-10" -> 10
        "Withdraw-*" -> player.getVar("last_bank_amount", 0)
        "Withdraw-All" -> Int.MAX_VALUE
        "Withdraw-All but one" -> item.amount - 1
        else -> return@on
    }
    withdraw(player, item, itemSlot, amount)
}

on<InterfaceOption>({ id == "bank" && component == "container" && option == "Withdraw-X" }) { player: Player ->
    player.dialogue {
        val amount = intEntry("Enter amount:")
        player.setVar("last_bank_amount", amount)
        withdraw(player, item, itemSlot, amount)
    }
}

on<InterfaceOption>({ id == "bank" && component == "note_mode" && option == "Toggle item/note withdrawl" }) { player: Player ->
    player.toggleVar("bank_notes")
}

fun withdraw(player: Player, item: Item, slot: Int, amount: Int) {
    if (player.action.type != ActionType.Bank || amount < 1) {
        return
    }

    var noted = item
    if (player.getVar("bank_notes", false)) {
        val note = item.noted
        if (note == null) {
            player.message("This item cannot be withdrawn as a note.")
        } else {
            noted = note
        }
    }
    player.bank.transaction {
        val removed = removeToLimit(item.id)
        link(player.inventory).add(noted.id, removed)
    }
    when (player.bank.transaction.error) {
        is TransactionError.Full -> player.message("Your inventory is full.")
        TransactionError.None -> {
            if (player.bank[slot].id != item.id) {
                val tab = Bank.getTab(player, slot)
                if (tab > 0) {
                    player.decVar("bank_tab_$tab")
                }
            }
        }
        else -> logger.info { "Bank withdraw issue: $player ${player.bank.transaction.error}" }
    }
}
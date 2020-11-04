package rs.dusk.world.activity.bank

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.contain.ContainerResult
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.item.detail.ItemDetails
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

BooleanVariable(115, Variable.Type.VARP, persistent = true).register("bank_notes")

val logger = InlineLogger()

val decoder: ItemDetails by inject()

InterfaceOption where { name == "bank" && component == "container" && option.startsWith("Withdraw") } then {
    val amount = when (option) {
        "Withdraw-1" -> 1
        "Withdraw-5" -> 5
        "Withdraw-10" -> 10
        "Withdraw-*" -> player.getVar("last_bank_amount", 0)
        "Withdraw-All" -> Int.MAX_VALUE
        "Withdraw-All but one" -> player.bank.getAmount(itemIndex) - 1
        else -> return@then
    }
    withdraw(player, itemId, itemIndex, amount)
}

InterfaceOption where { name == "bank" && component == "container" && option == "Withdraw-X" } then {
    player.dialogue {
        val amount = intEntry("Enter amount:")
        player.setVar("last_bank_amount", amount)
        withdraw(player, itemId, itemIndex, amount)
    }
}

InterfaceOption where { name == "bank" && component == "note_mode" && option == "Toggle item/note withdrawl" } then {
    player.toggleVar("bank_notes")
}

fun withdraw(player: Player, item: Int, slot: Int, amount: Int) {
    if (player.action.type != ActionType.Bank || amount < 1) {
        return
    }

    var itemId = item
    if(player.getVar("bank_notes", false)) {
        val def = decoder.get(item)
        if (def.noteId != -1) {
            itemId = def.noteId
        } else {
            player.message("This item cannot be withdrawn as a note.")
        }
    }

    val current = player.bank.getAmount(slot)
    var amount = amount
    if (amount > current) {
        amount = current
    }

    if (!player.bank.move(player.inventory, item, amount, slot, targetId = itemId)) {
        if (player.bank.result == ContainerResult.Full) {
            player.message("Your inventory is full.")
        } else {
            logger.info { "Bank withdraw issue: $player ${player.bank.result}" }
        }
    } else if (player.bank.getItem(slot) != item) {
        val tab = Bank.getTab(player, slot)
        if (tab > 0) {
            player.decVar("bank_tab_$tab")
        }
    }
}
package rs.dusk.world.activity.bank

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.client.variable.getVar
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

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

fun withdraw(player: Player, item: Int, slot: Int, amount: Int) {
    if(player.action.type != ActionType.Bank || amount < 1) {
        return
    }

    val current = player.bank.getAmount(slot)
    var amount = amount
    if (amount > current) {
        amount = current
    }

    if(!player.bank.move(player.inventory, item, amount, slot)) {
        player.message("Your inventory is full.")
    }
}
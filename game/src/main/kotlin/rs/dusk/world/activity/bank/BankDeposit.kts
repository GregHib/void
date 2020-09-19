package rs.dusk.world.activity.bank

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.set
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.activity.bank.Bank.sendLastDeposit
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "bank_side" && component == "container" && option.startsWith("Deposit") } then {
    val amount = when (option) {
        "Deposit-1" -> 1
        "Deposit-5" -> 5
        "Deposit-10" -> 10
        "Deposit-All" -> Int.MAX_VALUE
        "Deposit-${player["last_deposit", 0]}" -> player["last_deposit", 0]
        else -> return@then
    }
    deposit(player, itemId, itemIndex, amount)
}

InterfaceOption where { name == "bank_side" && component == "container" && option == "Deposit-X" } then {
    player.dialogue {
        val amount = intEntry("Enter amount:")
        player["last_deposit", true] = amount
        sendLastDeposit(player)
        deposit(player, itemId, itemIndex, amount)
    }
}

fun Player.banking() = action.type == ActionType.Bank

fun deposit(player: Player, item: Int, slot: Int, amount: Int) {
    if(!player.banking() || amount < 1) {
        return
    }

    val current = player.inventory.getCount(item).toInt()
    var amount = amount
    if (amount > current) {
        amount = current
    }

    if(!player.inventory.move(player.bank, item, amount, slot)) {
        player.message("Your bank is too full to deposit any more.")
    }
}
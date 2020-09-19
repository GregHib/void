package rs.dusk.world.activity.bank

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.getVar
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.contain.beastOfBurden
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

IntVariable(1249, Variable.Type.VARP, persistent = true, defaultValue = 0).register("last_bank_amount")

InterfaceOption where { name == "bank_side" && component == "container" && option.startsWith("Deposit") } then {
    val amount = when (option) {
        "Deposit-1" -> 1
        "Deposit-5" -> 5
        "Deposit-10" -> 10
        "Deposit-*" -> player.getVar("last_bank_amount", 0)
        "Deposit-All" -> Int.MAX_VALUE
        else -> return@then
    }
    deposit(player, itemId, itemIndex, amount)
}

InterfaceOption where { name == "bank_side" && component == "container" && option == "Deposit-X" } then {
    player.dialogue {
        val amount = intEntry("Enter amount:")
        player.setVar("last_bank_amount", amount)
        deposit(player, itemId, itemIndex, amount)
    }
}

fun deposit(player: Player, item: Int, slot: Int, amount: Int) {
    if(player.action.type != ActionType.Bank || amount < 1) {
        return
    }

    val current = player.inventory.getCount(item).toInt()
    var amount = amount
    if (amount > current) {
        amount = current
    }

    if(!player.inventory.move(player.bank, item, amount, slot)) {
        player.full()
    }
}

fun Player.full() = message("Your bank is too full to deposit any more.")

InterfaceOption where { name == "bank" && component == "carried" && option == "Deposit carried items" } then {
    if(player.inventory.isEmpty()) {
        player.message("You have no items in your inventory to deposit.")
    } else {
        if(!player.inventory.moveAll(player.bank)) {
            player.full()
        }
    }
}

InterfaceOption where { name == "bank" && component == "worn" && option == "Deposit worn items" } then {
    if(player.equipment.isEmpty()) {
        player.message("You have no equipped items to deposit.")
    } else {
        if(!player.equipment.moveAll(player.bank)) {
            player.full()
        }
    }
}

InterfaceOption where { name == "bank" && component == "burden" && option == "Deposit beast of burden inventory" } then {
    // TODO no familiar & no bob familiar messages
    if(player.beastOfBurden.isEmpty()) {
        player.message("Your familiar has no items to deposit.")
    } else {
        if(!player.beastOfBurden.moveAll(player.bank)) {
            player.full()
        }
    }
}
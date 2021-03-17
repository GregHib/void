package world.gregs.voidps.world.activity.bank

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.character.contain.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.bank.Bank.getIndexOfTab
import world.gregs.voidps.world.interact.dialogue.type.intEntry

IntVariable(1249, Variable.Type.VARP, persistent = true, defaultValue = 0).register("last_bank_amount")

val logger = InlineLogger()

val decoder: ItemDefinitions by inject()

InterfaceOption where { name == "bank_side" && component == "container" && option.startsWith("Deposit") } then {
    val amount = when (option) {
        "Deposit-1" -> 1
        "Deposit-5" -> 5
        "Deposit-10" -> 10
        "Deposit-*" -> player.getVar("last_bank_amount", 0)
        "Deposit-All" -> Int.MAX_VALUE
        else -> return@then
    }
    deposit(player, player.inventory, itemId, itemIndex, amount)
}

InterfaceOption where { name == "bank_side" && component == "container" && option == "Deposit-X" } then {
    player.dialogue {
        val amount = intEntry("Enter amount:")
        player.setVar("last_bank_amount", amount)
        deposit(player, player.inventory, itemId, itemIndex, amount)
    }
}

fun deposit(player: Player, container: Container, item: Int, slot: Int, amount: Int): Boolean {
    if (player.action.type != ActionType.Bank || amount < 1) {
        return true
    }

    val details = decoder.get(item)
    if (!details["bankable", true]) {
        player.message("This item cannot be banked.")
        return true
    }

    var itemId = item
    val def = decoder.get(item)
    if (def.notedTemplateId != -1) {
        if(def.noteId == -1) {
            logger.warn { "Issue depositing noted item $item" }
            return true
        }
        itemId = def.noteId
    }

    val current = container.getCount(item).toInt()
    var amount = amount
    if (amount > current) {
        amount = current
    }

    val tab = player.getVar("open_bank_tab", 1) - 1
    val targetIndex: Int? = if (tab > 0) getIndexOfTab(player, tab) + player.getVar("bank_tab_$tab", 0) else null
    if (!container.move(player.bank, item, amount, slot, targetIndex, true, itemId)) {
        if (player.bank.result == ContainerResult.Full) {
            player.full()
        } else {
            logger.info { "Bank deposit issue: $player ${player.bank.result}" }
        }
        return false
    } else if (tab > 0) {
        player.incVar("bank_tab_$tab")
    }
    return true
}

fun Player.full() = message("Your bank is too full to deposit any more.")

InterfaceOption where { name == "bank" && component == "carried" && option == "Deposit carried items" } then {
    if (player.inventory.isEmpty()) {
        player.message("You have no items in your inventory to deposit.")
    } else {
        bankAll(player, player.inventory)
    }
}

InterfaceOption where { name == "bank" && component == "worn" && option == "Deposit worn items" } then {
    if (player.equipment.isEmpty()) {
        player.message("You have no equipped items to deposit.")
    } else {
        bankAll(player, player.equipment)
    }
}

InterfaceOption where { name == "bank" && component == "burden" && option == "Deposit beast of burden inventory" } then {
    // TODO no familiar & no bob familiar messages
    if (player.beastOfBurden.isEmpty()) {
        player.message("Your familiar has no items to deposit.")
    } else {
        bankAll(player, player.beastOfBurden)
    }
}

fun bankAll(player: Player, container: Container) {
    for (index in container.getItems().indices.reversed()) {
        if (!container.isIndexFree(index) && !deposit(player, container, container.getItem(index), index, container.getAmount(index))) {
            break
        }
    }
}
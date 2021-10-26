package world.gregs.voidps.world.activity.bank

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.incVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.Bank.getIndexOfTab
import world.gregs.voidps.world.interact.dialogue.type.intEntry

val logger = InlineLogger()

on<InterfaceOption>({ id == "bank_side" && component == "container" && option.startsWith("Deposit") }) { player: Player ->
    val amount = when (option) {
        "Deposit-1" -> 1
        "Deposit-5" -> 5
        "Deposit-10" -> 10
        "Deposit-*" -> player.getVar("last_bank_amount", 0)
        "Deposit-All" -> Int.MAX_VALUE
        else -> return@on
    }
    deposit(player, player.inventory, item, itemIndex, amount)
}

on<InterfaceOption>({ id == "bank_side" && component == "container" && option == "Deposit-X" }) { player: Player ->
    player.dialogue {
        val amount = intEntry("Enter amount:")
        player.setVar("last_bank_amount", amount)
        deposit(player, player.inventory, item, itemIndex, amount)
    }
}

fun deposit(player: Player, container: Container, item: Item, slot: Int, amount: Int): Boolean {
    if (player.action.type != ActionType.Bank || amount < 1) {
        return true
    }

    if (!item.def["bankable", true]) {
        player.message("This item cannot be banked.")
        return true
    }

    val noted = item.toNote()
    if (noted == null) {
        logger.warn { "Issue depositing noted item $item" }
        return true
    }

    val current = container.getCount(item).toInt()
    var amount = amount
    if (amount > current) {
        amount = current
    }

    val tab = player.getVar("open_bank_tab", 1) - 1
    val targetIndex: Int? = if (tab > 0) getIndexOfTab(player, tab) + player.getVar("bank_tab_$tab", 0) else null
    if (!container.move(player.bank, item.id, amount, slot, targetIndex, true, noted.id)) {
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

fun bankAll(player: Player, container: Container) {
    for ((index, item) in container.getItems().withIndex().reversed()) {
        if (!container.isIndexFree(index) && !deposit(player, container, item, index, item.amount)) {
            break
        }
    }
}
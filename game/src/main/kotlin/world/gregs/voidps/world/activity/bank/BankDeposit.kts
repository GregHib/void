package world.gregs.voidps.world.activity.bank

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.incVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.beastOfBurden
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
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
    deposit(player, player.inventory, item, amount)
}

on<InterfaceOption>({ id == "bank_side" && component == "container" && option == "Deposit-X" }) { player: Player ->
    player.dialogue {
        val amount = intEntry("Enter amount:")
        player.setVar("last_bank_amount", amount)
        deposit(player, player.inventory, item, amount)
    }
}

fun deposit(player: Player, container: Container, item: Item, amount: Int): Boolean {
    if (player.action.type != ActionType.Bank || amount < 1) {
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

    val tab = player.getVar("open_bank_tab", 1) - 1
    if (tab > 0) {
        val targetIndex = getIndexOfTab(player, tab) + player.getVar("bank_tab_$tab", 0)
        container.insertTab(item.id, amount, player.bank, notNoted.id, targetIndex)
    } else {
        container.insertBank(item.id, amount, player.bank, notNoted.id)
    }
    when (container.transaction.error) {
        is TransactionError.Full -> player.full()
        TransactionError.None -> if (tab > 0) player.incVar("bank_tab_$tab")
        else -> logger.info { "Bank deposit issue: $player ${player.bank.transaction.error}" }
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
        if (!container.isIndexFree(index) && !deposit(player, container, item, item.amount)) {
            break
        }
    }
}

fun Container.insertBank(item: String, amount: Int, target: Container, targetItem: String) = transaction {
    val removed = removeToLimit(item, amount)
    val transaction = linkTransaction(target)
    if (!target.stackRule.stackable(targetItem)) {
        // Insert at the end of the bank
        transaction.addToLimit(targetItem, removed)
        return@transaction
    }
    // Check if item stack already exists
    val index = target.indexOf(targetItem)
    if (index == -1) {
        // Add to new stack at the end of the bank
        transaction.addToLimit(targetItem, removed)
        return@transaction
    }
    // Add to existing stack
    transaction.addToLimit(targetItem, removed)
}

fun Container.insertTab(item: String, amount: Int, target: Container, targetItem: String, targetIndex: Int) = transaction {
    val removed = removeToLimit(item, amount)
    val transaction = linkTransaction(target)
    if (!target.stackRule.stackable(targetItem)) {
        // Insert one-by-one into tab
        repeat(removed) {
            transaction.shiftInsert(targetItem, 1, targetIndex)
        }
        return@transaction
    }
    // Check if item stack already exists
    val index = target.indexOf(targetItem)
    if (index == -1) {
        // Add new stack to tab
        transaction.shiftInsert(targetItem, removed, targetIndex)
        return@transaction
    }
    // Add to existing stack
    transaction.addToLimit(targetItem, removed)
}
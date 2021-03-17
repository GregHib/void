package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.PlayerRegistered
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.utility.inject

ListVariable(304, Variable.Type.VARP, persistent = true, values = listOf(
    "swap",
    "insert"
)).register("bank_item_mode")
IntVariable(1038, Variable.Type.VARC).register("bank_spaces_used_free")
IntVariable(192, Variable.Type.VARC).register("bank_spaces_used_member")

PlayerRegistered then {
    player.bank.listeners.add {
        player.setVar("bank_spaces_used_free", player.bank.getFreeToPlayItemCount())
        player.setVar("bank_spaces_used_member", player.bank.count)
        player.bank.sort()
        player.sendContainer("bank")
    }
}

val decoder: ItemDefinitions by inject()

fun Container.getFreeToPlayItemCount(): Int {
    return getItems().count { !(decoder.getOrNull(it)?.members ?: true) }
}

InterfaceSwitch where { name == "bank" && component == "container" && toName == name && toComponent == component } then {
    when (player.getVar<String>("bank_item_mode")) {
        "swap" -> player.bank.swap(fromSlot, toSlot)
        "insert" -> moveItem(player, fromSlot, toSlot, null)
    }
}

InterfaceOption where { name == "bank" && component == "tab_1" && option == "View all" } then {
    player.setVar("open_bank_tab", 1)
}

InterfaceOption where { name == "bank" && component.startsWith("tab_") && option == "View Tab" } then {
    player.setVar("open_bank_tab", component.removePrefix("tab_").toInt())
}

InterfaceOption where { name == "bank" && component == "item_mode" && option == "Toggle swap/insert" } then {
    val value: String = player.getVar("bank_item_mode")
    player.setVar("bank_item_mode", if (value == "insert") "swap" else "insert")
}

InterfaceSwitch where { name == "bank" && component == "container" && toName == name && toComponent.startsWith("tab_") } then {
    val toTab = toComponent.removePrefix("tab_").toInt() - 1
    moveItem(player, fromSlot, null, toTab)
}

fun getLastTabIndex(player: Player, toTab: Int): Int {
    return (1..toTab).sumBy { tab -> player.getVar("bank_tab_$tab") }
}

fun moveItem(player: Player, fromSlot: Int, toSlot: Int?, toTab: Int?) {
    val fromTab = Bank.getTab(player, fromSlot)
    val toTab = toTab ?: Bank.getTab(player, toSlot!!)
    val moveToDifferentTab = fromTab != toTab
    val emptyTab = fromTab > 0 && moveToDifferentTab && player.decVar("bank_tab_$fromTab") <= 0
    when {
        toTab == Bank.mainTab -> insert(player, fromSlot, toSlot ?: player.bank.freeIndex())
        moveToDifferentTab -> {
            val index = toSlot ?: getLastTabIndex(player, toTab)
            if (toTab > 0) {
                player.incVar("bank_tab_$toTab")
            }
            insert(player, fromSlot, index)
        }
        // Move to the end of the same tab
        else -> insert(player, fromSlot, toSlot ?: (fromSlot + player.getVar("bank_tab_$fromTab", 0) - 1))
    }
    if (emptyTab) {
        nudgeTabsBackOne(player, fromTab)
    }
}

fun nudgeTabsBackOne(player: Player, from: Int) {
    for (i in from..Bank.tabCount) {
        player.setVar("bank_tab_$i", player.getVar("bank_tab_${i + 1}", 0))
    }
}

fun insert(player: Player, fromSlot: Int, toSlot: Int) {
    player.bank.move(fromSlot, player.bank, toSlot, insert = true)
}
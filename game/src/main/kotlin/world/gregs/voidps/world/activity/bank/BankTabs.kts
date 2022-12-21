package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.variable.decVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.incVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on

on<ItemChanged>({ player -> container == "bank" && player["sorting", false] }) { player: Player ->
    player.setVar("bank_spaces_used_free", player.bank.getFreeToPlayItemCount())
    player.setVar("bank_spaces_used_member", player.bank.count)
    player["sorting"] = true
    player.bank.sort()
    player.clear("sorting")
    player.sendContainer("bank")
}

fun Container.getFreeToPlayItemCount(): Int {
    return items.count { !it.def.members }
}

on<InterfaceSwitch>({ id == "bank" && component == "container" && toId == id && toComponent == component }) { player: Player ->
    when (player.getVar<String>("bank_item_mode")) {
        "swap" -> player.bank.swap(fromSlot, toSlot)
        "insert" -> moveItem(player, fromSlot, toSlot, null)
    }
}

on<InterfaceOption>({ id == "bank" && component == "tab_1" && option == "View all" }) { player: Player ->
    player.setVar("open_bank_tab", 1)
}

on<InterfaceOption>({ id == "bank" && component.startsWith("tab_") && option == "View Tab" }) { player: Player ->
    player.setVar("open_bank_tab", component.removePrefix("tab_").toInt())
}

on<InterfaceOption>({ id == "bank" && component == "item_mode" && option == "Toggle swap/insert" }) { player: Player ->
    val value: String = player.getVar("bank_item_mode")
    player.setVar("bank_item_mode", if (value == "insert") "swap" else "insert")
}

on<InterfaceSwitch>({ id == "bank" && component == "container" && toId == id && toComponent.startsWith("tab_") }) { player: Player ->
    val toTab = toComponent.removePrefix("tab_").toInt() - 1
    moveItem(player, fromSlot, null, toTab)
}

fun getLastTabIndex(player: Player, toTab: Int): Int {
    return (1..toTab).sumOf { tab -> player.getVar<Int>("bank_tab_$tab") }
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
    player.bank.shiftInsert(fromSlot, player.bank, toSlot)
    player.bank.sort()
}
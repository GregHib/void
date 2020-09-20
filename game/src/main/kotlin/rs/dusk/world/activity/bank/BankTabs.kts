package rs.dusk.world.activity.bank

import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.contain.sendContainer
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerSpawn
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption
import rs.dusk.world.interact.entity.player.display.InterfaceSwitch

ListVariable(304, Variable.Type.VARP, persistent = true, values = listOf(
    "swap",
    "insert"
)).register("bank_item_mode")

val tabCount = 8
val mainTab = 0

PlayerSpawn then {
    player.bank.listeners.add {
        player.bank.sort()
        player.sendContainer("bank")
    }
}

InterfaceSwitch where { name == "bank" && component == "container" && toName == name && toComponent == component } then {
    when (player.getVar<String>("bank_item_mode")) {
        "swap" -> player.bank.swap(fromSlot, toSlot)
        "insert" -> moveItem(player, fromSlot, toSlot, null)
    }
}

InterfaceOption where { name == "bank" && component == "item_mode" && option == "Toggle swap/insert" } then {
    val value: String = player.getVar("bank_item_mode")
    player.setVar("bank_item_mode", if (value == "insert") "swap" else "insert")
}

InterfaceSwitch where { name == "bank" && component == "container" && toName == name && toComponent.startsWith("tab_") } then {
    val toTab = toComponent.removePrefix("tab_").toInt() - 1
    println("Move $toTab")
    moveItem(player, fromSlot, null, toTab)
}

fun getLastTabIndex(player: Player, toTab: Int): Int {
    return (1..toTab).sumBy { tab -> player.getVar("bank_tab_$tab") }
}

fun moveItem(player: Player, fromSlot: Int, toSlot: Int?, toTab: Int?) {
    val fromTab = getTab(player, fromSlot)
    val toTab = toTab ?: getTab(player, toSlot!!)
    val moveToDifferentTab = fromTab != toTab
    val emptyTab = fromTab != -1 && moveToDifferentTab && player.decVar("bank_tab_$fromTab") <= 0
    when {
        toTab == mainTab -> insert(player, fromSlot, player.bank.freeIndex())
        moveToDifferentTab -> {
            val index = toSlot ?: getLastTabIndex(player, toTab)
            player.incVar("bank_tab_$toTab")
            insert(player, fromSlot, index)
        }
        // Move to the end of the same tab
        else -> insert(player, fromSlot, fromSlot + player.getVar<Int>("bank_tab_$fromTab") - 1)
    }
    if (emptyTab) {
        nudgeTabsBackOne(player, fromTab)
    }
}

fun nudgeTabsBackOne(player: Player, from: Int) {
    for (i in from..tabCount) {
        player.setVar("bank_tab_$i", player.getVar("bank_tab_${i + 1}", 0))
    }
}

fun insert(player: Player, fromSlot: Int, toSlot: Int) {
    player.bank.move(fromSlot, player.bank, toSlot, insert = true)
}

fun getTab(player: Player, slot: Int): Int {
    var total = 0
    for (tab in mainTab + 1..tabCount) {
        val count: Int = player.getVar("bank_tab_$tab")
        if (count == 0) {
            continue
        }
        total += count
        if (slot < total) {
            return tab
        }
    }
    return -1
}
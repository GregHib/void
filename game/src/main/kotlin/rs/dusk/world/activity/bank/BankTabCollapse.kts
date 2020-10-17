package rs.dusk.world.activity.bank

import rs.dusk.engine.client.variable.getVar
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.activity.bank.Bank.firstTab
import rs.dusk.world.activity.bank.Bank.getIndexOfTab
import rs.dusk.world.activity.bank.Bank.tabCount
import rs.dusk.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "bank" && component.startsWith("tab_") && option == "Collapse" } then {
    val tab = component.removePrefix("tab_").toInt() - 1
    val tabIndex = getIndexOfTab(player, tab)
    val count: Int = player.getVar("bank_tab_$tab", 0)

    moveItems(player, tabIndex, count)

    shiftTabs(player, tab)
}


fun moveItems(player: Player, tabIndex: Int, count: Int) {
    for (index in tabIndex + count - 1 downTo tabIndex) {
        player.bank.move(index, player.bank, player.bank.freeIndex())
    }
}

fun shiftTabs(player: Player, tab: Int) {
    for (t in tab + firstTab..tabCount) {
        player.setVar("bank_tab_${t - 1}", player.getVar("bank_tab_$t", 0))
    }
}
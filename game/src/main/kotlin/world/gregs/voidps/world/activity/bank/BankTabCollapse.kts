package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.world.activity.bank.Bank.firstTab
import world.gregs.voidps.world.activity.bank.Bank.getIndexOfTab
import world.gregs.voidps.world.activity.bank.Bank.tabCount
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

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
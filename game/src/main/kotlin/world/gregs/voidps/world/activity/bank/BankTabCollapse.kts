package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.transact.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.Bank.firstTab
import world.gregs.voidps.world.activity.bank.Bank.getIndexOfTab
import world.gregs.voidps.world.activity.bank.Bank.tabCount

on<InterfaceOption>({ id == "bank" && component.startsWith("tab_") && option == "Collapse" }) { player: Player ->
    val tab = component.removePrefix("tab_").toInt() - 1
    val tabIndex = getIndexOfTab(player, tab)
    val count: Int = player.getVar("bank_tab_$tab", 0)

    moveItems(player, tabIndex, count)

    shiftTabs(player, tab)
}


fun moveItems(player: Player, tabIndex: Int, count: Int) {
    for (index in tabIndex + count - 1 downTo tabIndex) {
        player.bank.move(index, player.bank.freeIndex())
    }
}

fun shiftTabs(player: Player, tab: Int) {
    for (t in tab + firstTab..tabCount) {
        player.setVar("bank_tab_${t - 1}", player.getVar("bank_tab_$t", 0))
    }
}
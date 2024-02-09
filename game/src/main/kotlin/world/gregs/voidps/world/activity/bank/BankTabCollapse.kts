package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.world.activity.bank.Bank.tabIndex

interfaceOption("Collapse", "tab_#", "bank") {
    val tab = component.removePrefix("tab_").toInt() - 1
    val tabIndex = tabIndex(player, tab)
    val count: Int = player["bank_tab_$tab", 0]
    val collapsed = player.bank.transaction {
        repeat(count) {
            shiftToFreeIndex(tabIndex)
        }
    }
    if (collapsed) {
        repeat(count) {
            Bank.decreaseTab(player, tab)
        }
    }
}
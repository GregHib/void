package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.Bank.tabIndex

on<InterfaceOption>({ id == "bank" && component.startsWith("tab_") && option == "Collapse" }) { player: Player ->
    val tab = component.removePrefix("tab_").toInt() - 1
    val tabIndex = tabIndex(player, tab)
    val count: Int = player.getVar("bank_tab_$tab", 0)
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
package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
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

        val current = player.getVar("open_bank_tab", 0)
        val lastIndex = (Bank.tabs).first { player.getVar("bank_tab_${it}", 0) == 0 }
        if (current > lastIndex) {
            player.setVar("open_bank_tab", 1)
        }
    }
}
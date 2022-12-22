package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.incVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.shift
import world.gregs.voidps.engine.entity.character.contain.swap
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<ItemChanged>({ container == "bank" }) { player: Player ->
    player.setVar("bank_spaces_used_free", player.bank.getFreeToPlayItemCount())
    player.setVar("bank_spaces_used_member", player.bank.count)
}

fun Container.getFreeToPlayItemCount(): Int {
    return items.count { it.isNotEmpty() && !it.def.members }
}

on<InterfaceSwitch>({ id == "bank" && component == "container" && toId == id && toComponent == component }) { player: Player ->
    when (player.getVar<String>("bank_item_mode")) {
        "swap" -> player.bank.swap(fromSlot, toSlot)
        "insert" -> {
            val fromTab = Bank.getTab(player, fromSlot)
            val toTab = Bank.getTab(player, toSlot)
            shiftTab(player, fromSlot, toSlot, fromTab, toTab)
        }
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
    val fromTab = Bank.getTab(player, fromSlot)
    val toTab = toComponent.removePrefix("tab_").toInt() - 1
    val toIndex = if (toTab == Bank.mainTab) player.bank.freeIndex() else Bank.tabIndex(player, toTab + 1)
    shiftTab(player, fromSlot, toIndex, fromTab, toTab)
}

/*
    Move to index in same tab -> shiftInsert
 */
fun shiftTab(player: Player, fromIndex: Int, toIndex: Int, fromTab: Int, toTab: Int) {
    val moved = fromTab != toTab
    // Increase count of target tab
    if (moved && toTab > 0) {
        player.incVar("bank_tab_$toTab")
    }
    if (moved || toTab == Bank.mainTab) {
        Bank.decreaseTab(player, fromTab)
    }
    // Remove one from target index to include this item's own position change
    player.bank.shift(fromIndex, if (fromIndex < toIndex) toIndex - 1 else toIndex)
}
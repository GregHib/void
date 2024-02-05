package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.interfaceSwitch
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.itemChanged
import world.gregs.voidps.engine.inv.shift
import world.gregs.voidps.engine.inv.swap

itemChanged({ inventory == "bank" }) { player: Player ->
    player["bank_spaces_used_free"] = player.bank.getFreeToPlayItemCount()
    player["bank_spaces_used_member"] = player.bank.count
}

fun world.gregs.voidps.engine.inv.Inventory.getFreeToPlayItemCount(): Int {
    return items.count { it.isNotEmpty() && !it.def.members }
}

interfaceSwitch({ id == "bank" && component == "inventory" && toId == id && toComponent == component }) { player: Player ->
    when (player["bank_item_mode", "swap"]) {
        "swap" -> player.bank.swap(fromSlot, toSlot)
        "insert" -> {
            val fromTab = Bank.getTab(player, fromSlot)
            val toTab = Bank.getTab(player, toSlot)
            shiftTab(player, fromSlot, toSlot, fromTab, toTab)
        }
    }
}

interfaceOption({ id == "bank" && component == "tab_1" && option == "View all" }) { player: Player ->
    player["open_bank_tab"] = 1
}

interfaceOption({ id == "bank" && component.startsWith("tab_") && option == "View Tab" }) { player: Player ->
    player["open_bank_tab"] = component.removePrefix("tab_").toInt()
}

interfaceOption({ id == "bank" && component == "item_mode" && option == "Toggle swap/insert" }) { player: Player ->
    val value: String = player["bank_item_mode", "swap"]
    player["bank_item_mode"] = if (value == "insert") "swap" else "insert"
}

interfaceSwitch({ id == "bank" && component == "inventory" && toId == id && toComponent.startsWith("tab_") }) { player: Player ->
    val fromTab = Bank.getTab(player, fromSlot)
    val toTab = toComponent.removePrefix("tab_").toInt() - 1
    val toIndex = if (toTab == Bank.MAIN_TAB) player.bank.freeIndex() else Bank.tabIndex(player, toTab + 1)
    shiftTab(player, fromSlot, toIndex, fromTab, toTab)
}

/*
    Move to index in same tab -> shiftInsert
 */
fun shiftTab(player: Player, fromIndex: Int, toIndex: Int, fromTab: Int, toTab: Int) {
    val moved = fromTab != toTab
    // Increase count of target tab
    if (moved && toTab > 0) {
        player.inc("bank_tab_$toTab")
    }
    if (moved || toTab == Bank.MAIN_TAB) {
        Bank.decreaseTab(player, fromTab)
    }
    // Remove one from target index to include this item's own position change
    player.bank.shift(fromIndex, if (fromIndex < toIndex) toIndex - 1 else toIndex)
}
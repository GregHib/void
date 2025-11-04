package content.entity.player.bank

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.shift
import world.gregs.voidps.engine.inv.swap

class BankTabs : Script {

    init {
        inventoryUpdated("bank") { _, _ ->
            set("bank_spaces_used_free", bank.countFreeToPlayItems())
            set("bank_spaces_used_member", bank.count)
        }

        interfaceSwap("bank:inventory") { _, _, fromSlot, toSlot ->
            when (get("bank_item_mode", "swap")) {
                "swap" -> bank.swap(fromSlot, toSlot)
                "insert" -> {
                    val fromTab = Bank.getTab(this, fromSlot)
                    val toTab = Bank.getTab(this, toSlot)
                    shiftTab(this, fromSlot, toSlot, fromTab, toTab)
                }
            }
        }

        interfaceOption("View all", "bank:tab_1") {
            set("open_bank_tab", 1)
        }

        interfaceOption("View Tab", "bank:tab_#") {
            set("open_bank_tab", it.component.removePrefix("tab_").toInt())
        }

        interfaceOption("Toggle swap/insert", "bank:item_mode") {
            val value: String = get("bank_item_mode", "swap")
            set("bank_item_mode", if (value == "insert") "swap" else "insert")
        }

        interfaceSwap("bank:tab_#") { _, toId, fromSlot, _ ->
            val fromTab = Bank.getTab(this, fromSlot)
            val toTab = toId.substringAfter(":").removePrefix("tab_").toInt() - 1
            val toIndex = if (toTab == Bank.MAIN_TAB) bank.freeIndex() else Bank.tabIndex(this, toTab + 1)
            shiftTab(this, fromSlot, toIndex, fromTab, toTab)
        }
    }

    fun Inventory.countFreeToPlayItems(): Int = items.count { it.isNotEmpty() && !it.def.members }

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
}

package content.entity.player.bank

import content.entity.player.bank.Bank.tabIndex
import world.gregs.voidps.engine.Script

class BankTabCollapse : Script {

    init {
        interfaceOption("Collapse", "bank:tab_#") {
            val tab = it.component.removePrefix("tab_").toInt() - 1
            val tabIndex = tabIndex(this, tab)
            val count: Int = get("bank_tab_$tab", 0)
            val lastIndex = bank.count - 1
            val collapsed = bank.transaction {
                // Save the tab items being collapsed
                val tabItems = (0 until count).map { inventory[tabIndex + it] }
                // Shift all items after the tab left by count to close the gap
                for (i in tabIndex until lastIndex - count + 1) {
                    set(i, inventory[i + count])
                }
                // Place the collapsed tab items at the end
                for (i in tabItems.indices) {
                    set(lastIndex - count + 1 + i, tabItems[i])
                }
            }
            if (collapsed) {
                repeat(count) {
                    Bank.decreaseTab(this, tab)
                }
            }
        }
    }
}

package content.entity.player.bank

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.holdsItem

object Bank {
    private const val TAB_COUNT = 8
    const val MAIN_TAB = 0
    private const val FIRST_TAB = MAIN_TAB + 1
    val tabs = FIRST_TAB..TAB_COUNT

    fun getTab(player: Player, slot: Int): Int {
        val max = player.bank.count
        var total = 0
        for (tab in tabs) {
            val count: Int = player["bank_tab_$tab", 0]
            if (count == 0) {
                continue
            }
            total += count
            if (slot < total) {
                return tab
            }
        }
        return if (slot <= max) MAIN_TAB else -1
    }

    fun decreaseTab(player: Player, tab: Int) {
        // Reduce count of tab removed from
        if (tab <= MAIN_TAB || player.dec("bank_tab_$tab") > 0) {
            return
        }
        var lastTab = -1
        // Shift all tabs after it left by one, if tab is emptied
        for (i in tab..TAB_COUNT) {
            val next = player["bank_tab_${i + 1}", 0]
            if (next == 0 && lastTab == -1) {
                lastTab = i
            }
            player["bank_tab_$i"] = next
        }

        val current = player["open_bank_tab", 0]
        if (current > lastTab) {
            player["open_bank_tab"] = lastTab
        }
    }

    fun tabIndex(player: Player, tab: Int): Int = (1 until tab).sumOf { t -> player.get<Int>("bank_tab_$t", 0) }
}

val Player.bank: Inventory
    get() = inventories.inventory("bank")

fun Player.ownsItem(id: String) = holdsItem(id) || bank.contains(id)

fun Player.ownsItem(id: String, amount: Int) = holdsItem(id, amount) || bank.contains(id, amount)

val Item.isNote: Boolean
    get() = def.notedTemplateId != -1

val Item.noted: Item?
    get() = if (def.noteId != -1) {
        val definition = get<ItemDefinitions>().get(def.noteId)
        copy(id = definition.stringId)
    } else if (def.notedTemplateId != -1) {
        null
    } else {
        this
    }

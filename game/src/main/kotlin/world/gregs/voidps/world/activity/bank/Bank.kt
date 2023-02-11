package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.variable.decVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get

object Bank {
    const val tabCount = 8
    const val mainTab = 0
    const val firstTab = mainTab + 1
    val tabs = firstTab..tabCount

    fun getTab(player: Player, slot: Int): Int {
        val max = player.bank.count
        var total = 0
        for (tab in tabs) {
            val count: Int = player.getVar("bank_tab_$tab")
            if (count == 0) {
                continue
            }
            total += count
            if (slot < total) {
                return tab
            }
        }
        return if (slot <= max) mainTab else -1
    }

    fun decreaseTab(player: Player, tab: Int) {
        // Reduce count of tab removed from
        if (tab <= mainTab || player.decVar("bank_tab_$tab") > 0) {
            return
        }
        var lastTab = -1
        // Shift all tabs after it left by one, if tab is emptied
        for (i in tab..tabCount) {
            val next = player.getVar("bank_tab_${i + 1}", 0)
            if (next == 0 && lastTab == -1) {
                lastTab = i
            }
            player.setVar("bank_tab_$i", next)
        }

        val current = player.getVar("open_bank_tab", 0)
        if (current > lastTab) {
            player.setVar("open_bank_tab", lastTab)
        }
    }

    fun tabIndex(player: Player, tab: Int): Int {
        return (1 until tab).sumOf { t -> player.getVar<Int>("bank_tab_$t") }
    }
}

val Player.bank: Container
    get() = containers.container("bank")

fun Player.hasBanked(id: String) = hasItem(id) || bank.contains(id)

fun Player.hasBanked(id: String, amount: Int) = hasItem(id, amount) || bank.contains(id, amount)

val Item.isNote: Boolean
    get() = def.notedTemplateId != -1

val Item.noted: Item?
    get() = if (def.noteId != -1) {
        copy(id = get<ItemDefinitions>().get(def.noteId).stringId)
    } else if (def.notedTemplateId != -1) {
        null
    } else {
        this
    }

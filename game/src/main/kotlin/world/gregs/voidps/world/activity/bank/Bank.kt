package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.get

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

    fun getIndexOfTab(player: Player, tab: Int): Int {
        var index = 0
        for (t in tabs) {
            val count: Int = player.getVar("bank_tab_$t")
            if (count == 0) {
                continue
            }
            if (t == tab) {
                return index
            }
            index += count
        }
        return if (tab == 0) index else -1
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

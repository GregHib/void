package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.has
import world.gregs.voidps.engine.entity.character.player.Player

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
    get() = container("bank")

fun Player.has(item: String, banked: Boolean) = has(item) || (banked && bank.contains(item))
package content.social.trade

import content.entity.player.dialogue.type.intEntry
import content.social.trade.Trade.getPartner
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.inv.transact.operation.SwapItem.swap

/**
 * Offering an item to lend for a duration
 */

class TradeLending : Script {

    val lendRestriction = object : ItemRestrictionRule {
        override fun restricted(id: String) = ItemDefinitions.get(id).lendId == -1
    }

    init {
        playerSpawn {
            loan.itemRule = lendRestriction
        }

        interfaceOption("Specify", "trade_main:loan_time") {
            val hours = intEntry("Set the loan duration in hours: (1 - 72)<br>(Enter <col=7f0000>0</col> for 'Just until logout'.)").coerceIn(0, 72)
            setLend(this, hours)
        }

        interfaceOption("‘Until Logout‘", "trade_main:loan_time") {
            setLend(this, 0)
        }

        interfaceOption("Lend", "trade_side:offer") { (item, itemSlot) ->
            val partner = getPartner(this) ?: return@interfaceOption
            lend(this, partner, item.id, itemSlot)
        }
    }

    fun setLend(player: Player, time: Int) {
        player["lend_time"] = time
        val partner = getPartner(player) ?: return
        partner["other_lend_time"] = time
    }

    fun lend(player: Player, other: Player, id: String, slot: Int) {
        if (!Trade.isTrading(player, 1)) {
            return
        }
        if (player.loanReturnedItems.isFull()) {
            player.message("You are already lending an item, you can't lend another.")
            return
        }

        if (other.contains("borrowed_item")) {
            player.message("They are already borrowing an item and can't borrow another.")
            return
        }

        // Item must have a lent version
        if (player.loan.restricted(id)) {
            player.message("That item cannot be lent.")
            return
        }

        val lent = player.inventory.transaction {
            swap(slot, player.loan, 0)
        }
        if (!lent) {
            player.message("That item cannot be lent.")
            return
        }
        player["lend_time"] = 0
        other["other_lend_time"] = 0
    }
}

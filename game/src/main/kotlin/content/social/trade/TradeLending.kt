package content.social.trade

import content.entity.player.dialogue.type.intEntry
import content.social.trade.Trade.getPartner
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.inv.transact.operation.SwapItem.swap

/**
 * Offering an item to lend for a duration
 */

@Script
class TradeLending : Api {

    val definitions: ItemDefinitions by inject()

    val lendRestriction = object : ItemRestrictionRule {
        override fun restricted(id: String) = definitions.get(id).lendId == -1
    }

    override fun spawn(player: Player) {
        player.loan.itemRule = lendRestriction
    }

    init {
        interfaceOption("Specify", "loan_time", "trade_main") {
            val hours = intEntry("Set the loan duration in hours: (1 - 72)<br>(Enter <col=7f0000>0</col> for 'Just until logout'.)").coerceIn(0, 72)
            setLend(player, hours)
        }

        interfaceOption("‘Until Logout‘", "loan_time", "trade_main") {
            setLend(player, 0)
        }

        interfaceOption("Lend", "offer", "trade_side") {
            val partner = getPartner(player) ?: return@interfaceOption
            lend(player, partner, item.id, itemSlot)
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
        if (player.returnedItems.isFull()) {
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

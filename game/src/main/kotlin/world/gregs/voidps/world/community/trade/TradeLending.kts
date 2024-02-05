package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.interact.dialogue.type.intEntry

/**
 * Offering an item to lend for a duration
 */

val definitions: ItemDefinitions by inject()

// Item must have a lent version
val lendRestriction = object : ItemRestrictionRule {
    override fun restricted(id: String) = definitions.get(id).lendId == -1
}

playerSpawn { player: Player ->
    player.loan.itemRule = lendRestriction
}

interfaceOption({ id == "trade_main" && component == "loan_time" && option == "Specify" }) { player: Player ->
    val hours = intEntry("Set the loan duration in hours: (1 - 72)<br>(Enter <col=7f0000>0</col> for 'Just until logout'.)").coerceIn(0, 72)
    setLend(player, hours)
}

interfaceOption({ id == "trade_main" && component == "loan_time" && option == "‘Until Logout‘" }) { player: Player ->
    setLend(player, 0)
}

fun setLend(player: Player, time: Int) {
    player["lend_time"] = time
    val partner = getPartner(player) ?: return
    partner["other_lend_time"] = time
}

interfaceOption({ id == "trade_side" && component == "offer" && option == "Lend" }) { player: Player ->
    val partner = getPartner(player) ?: return@interfaceOption
    lend(player, partner, item.id, itemSlot)
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
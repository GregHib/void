import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.moveAll
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.trade.lend.Loan
import world.gregs.voidps.world.community.trade.lend.Loan.getTimeRemaining
import world.gregs.voidps.world.community.trade.lent

on<InterfaceOpened>({ id == "collection_box" }) { player: Player ->
    val lentItem: String? = player.getOrNull("lent_item")
    if (lentItem != null) {
        player.containers.container("lent_collection_box").transaction {
            set(0, Item(lentItem, 1))
        }
        val time = getTimeRemaining(player, "lend_timeout")
        if (time < 0) {
            player["lent_item_id"] = ""
            player["lent_item_amount"] = 0
        }
    }
}

on<InterfaceOption>({ id == "collection_box" && component == "box_lent" && option == "*" }) { player: Player ->
    if (!player.contains("lend_timeout")) {
        // Force reclaim
    } else {
        val remainder = getTimeRemaining(player, "lend_timeout")
        if (remainder > 0) {
            player.message("Loan expires ${Loan.getExpiry(player, "lend_timeout")}")
        } else {
            if (!player.lent.moveAll(player.inventory)) {
                player.inventory
            }
        }
    }
    /*
        if until logout
            demand = force reclaim
        else if time remaining
            "x time left"
        else
            Give item
     */
}
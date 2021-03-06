import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.getOrNull
import world.gregs.voidps.engine.entity.character.has
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.world.community.trade.lend.Loan
import world.gregs.voidps.world.community.trade.lend.Loan.getTimeRemaining
import world.gregs.voidps.world.community.trade.lent
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

val logger = InlineLogger()

IntVariable(1267, Variable.Type.VARP).register("lent_item")
IntVariable(1269, Variable.Type.VARP).register("lent_item_amount")

InterfaceOpened where { name == "collection_box" } then {
    val lentItem: Int? = player.getOrNull("lent_item")
    if(lentItem != null) {
        player.container("lent_collection_box").set(0, lentItem)
        val time = getTimeRemaining(player, "lend_timeout")
        if(time < 0) {
            player.setVar("lent_item", -1)
            player.setVar("lent_item_amount", 0)
        }
    }
}

InterfaceOption where { name == "collection_box" && component == "box_lent" && option == "*" } then {

    if(!player.has("lend_timeout")) {
        // Force reclaim
    } else {
        val remainder = getTimeRemaining(player, "lend_timeout")
        if(remainder > 0) {
            player.message("Loan expires ${Loan.getExpiry(player, "lend_timeout")}")
        } else {
            if(!player.lent.moveAll(player.inventory)) {
                player.inventory
            }
        }
    }
    /*
        if until logout
            demand = force reclaim
        else if time remaining
            "bla bla time left"
        else
            Give item
     */
}
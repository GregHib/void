import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.getOrNull
import rs.dusk.engine.entity.character.has
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.community.trade.lend.Loan
import rs.dusk.world.community.trade.lend.Loan.getTimeRemaining
import rs.dusk.world.community.trade.lent
import rs.dusk.world.interact.entity.player.display.InterfaceOption

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
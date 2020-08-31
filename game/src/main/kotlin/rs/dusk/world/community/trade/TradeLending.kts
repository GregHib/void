import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.community.trade.Trade.getPartner
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * Item lending duration modifying
 */

IntVariable(5026, Variable.Type.VARBIT).register("lend_time")
IntVariable(5070, Variable.Type.VARBIT).register("other_lend_time")

InterfaceOption where { name == "trade_main" && component == "loan_time" && option == "Specify" } then {
    player.dialogue {
        val hours = intEntry("Set the loan duration in hours: (1 - 72)<br>(Enter <col=7f0000>0</col> for 'Just until logout'.)").coerceIn(0, 72)
        setLend(player, hours)
    }
}

InterfaceOption where { name == "trade_main" && component == "loan_time" && option == "‘Until Logout‘" } then {
    setLend(player, 0)
}

fun setLend(player: Player, time: Int) {
    player.setVar("lend_time", time)
    val partner = getPartner(player) ?: return
    partner.setVar("other_lend_time", time)
}
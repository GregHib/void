package world.gregs.void.world.community.trade

import world.gregs.void.engine.client.ui.dialogue.dialogue
import world.gregs.void.engine.client.variable.IntVariable
import world.gregs.void.engine.client.variable.Variable
import world.gregs.void.engine.client.variable.setVar
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.world.community.trade.Trade.getPartner
import world.gregs.void.world.interact.dialogue.type.intEntry
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

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
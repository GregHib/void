package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.interact.dialogue.type.intEntry

/**
 * Item lending duration modifying
 */

on<InterfaceOption>({ id == "trade_main" && component == "loan_time" && option == "Specify" }) { player: Player ->
    val hours = intEntry("Set the loan duration in hours: (1 - 72)<br>(Enter <col=7f0000>0</col> for 'Just until logout'.)").coerceIn(0, 72)
    setLend(player, hours)
}

on<InterfaceOption>({ id == "trade_main" && component == "loan_time" && option == "‘Until Logout‘" }) { player: Player ->
    setLend(player, 0)
}

fun setLend(player: Player, time: Int) {
    player.set("lend_time", time)
    val partner = getPartner(player) ?: return
    partner.set("other_lend_time", time)
}
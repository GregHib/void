package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.Trade.isTradeInterface

/**
 * Declining or closing cancels the trade
 */

fun isDecline(component: String, option: String) = component == "decline" && option == "Decline"

fun isClose(component: String, option: String) = component == "close" && option == "Close"

on<InterfaceOption>({ isTradeInterface(id) && (isDecline(component, option) || isClose(component, option)) }) { player: Player ->
    val other = getPartner(player)
    player.message("Declined trade.", ChatType.Trade)
    player.closeMenu()
    other?.message("Other player declined trade.", ChatType.Trade)
}

on<Unregistered>({ isTradeInterface(it.menu) }) { player: Player ->
    val other = getPartner(player)
    player.closeMenu()
    other?.message("Other player declined trade.", ChatType.Trade)
}
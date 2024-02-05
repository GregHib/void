package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.Trade.isTradeInterface

/**
 * Declining or closing cancels the trade
 */

fun isDecline(component: String, option: String) = component == "decline" && option == "Decline"

fun isClose(component: String, option: String) = component == "close" && option == "Close"

interfaceOption({ isTradeInterface(id) && (isDecline(component, option) || isClose(component, option)) }) { player: Player ->
    val other = getPartner(player)
    player.message("Declined trade.", ChatType.Trade)
    player.closeMenu()
    other?.message("Other player declined trade.", ChatType.Trade)
}

playerDespawn({ isTradeInterface(it.menu) }) { player: Player ->
    val other = getPartner(player)
    player.closeMenu()
    other?.message("Other player declined trade.", ChatType.Trade)
}
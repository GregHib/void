package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.Trade.isTradeInterface

/**
 * Declining or closing cancels the trade
 */

interfaceOption("Decline", "decline", "trade_*") {
    val other = getPartner(player)
    player.message("Declined trade.", ChatType.Trade)
    other?.message("Other player declined trade.", ChatType.Trade)
    player.closeMenu()
    other?.closeMenu()
}

interfaceOption("Close", "close", "trade_*") {
    val other = getPartner(player)
    player.message("Declined trade.", ChatType.Trade)
    other?.message("Other player declined trade.", ChatType.Trade)
    player.closeMenu()
    other?.closeMenu()
}

playerDespawn { player ->
    if (isTradeInterface(player.menu)) {
        val other = getPartner(player)
        player.closeMenu()
        other?.message("Other player declined trade.", ChatType.Trade)
        other?.closeMenu()
    }
}
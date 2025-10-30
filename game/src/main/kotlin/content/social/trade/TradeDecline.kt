package content.social.trade

import content.social.trade.Trade.getPartner
import content.social.trade.Trade.isTradeInterface
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class TradeDecline : Script {

    init {
        interfaceOption("Decline", "decline", "trade_main") {
            decline()
        }

        interfaceOption("Decline", "decline", "trade_confirm") {
            decline()
        }

        interfaceOption("Close", "close", "trade_main") {
            decline()
        }

        interfaceOption("Close", "close", "trade_confirm") {
            decline()
        }

        playerDespawn {
            if (isTradeInterface(menu)) {
                val other = getPartner(this)
                closeMenu()
                other?.message("Other player declined trade.", ChatType.Trade)
                other?.closeMenu()
            }
        }
    }

    /**
     * Declining or closing cancels the trade
     */

    fun InterfaceOption.decline() {
        val other = getPartner(player)
        player.message("Declined trade.", ChatType.Trade)
        other?.message("Other player declined trade.", ChatType.Trade)
        player.closeMenu()
        other?.closeMenu()
    }
}

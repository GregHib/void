package content.social.trade

import content.social.trade.Trade.getPartner
import content.social.trade.Trade.isTradeInterface
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class TradeDecline : Script {

    init {
        interfaceOption("Decline", "trade_main:decline") {
            decline()
        }

        interfaceOption("Decline", "trade_confirm:decline") {
            decline()
        }

        interfaceOption("Close", "trade_main:close") {
            decline()
        }

        interfaceOption("Close", "trade_confirm:close") {
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

    fun Player.decline() {
        val other = getPartner(this)
        message("Declined trade.", ChatType.Trade)
        other?.message("Other player declined trade.", ChatType.Trade)
        closeMenu()
        other?.closeMenu()
    }
}

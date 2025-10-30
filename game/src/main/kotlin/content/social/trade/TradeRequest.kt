package content.social.trade

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.social.friend.friend
import content.social.trade.Trade.getPartner
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.entity.character.player.req.request
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.moveAll

class TradeRequest : Script {

    init {
        playerOperate("Trade with") { (target) ->
            val filter = target["trade_filter", "on"]
            if (filter == "off" || (filter == "friends" && !target.friend(this))) {
                return@playerOperate
            }
            if (target.hasRequest(this, "trade")) {
                message("Sending trade offer...", ChatType.Trade)
            } else {
                message("Sending trade offer...", ChatType.Trade)
                target.message("wishes to trade with you.", ChatType.TradeRequest, name = name)
            }
            request(target, "trade") { requester, acceptor ->
                startTrade(requester, acceptor)
                startTrade(acceptor, requester)
            }
        }

        interfaceClose("trade_main", "trade_confirm") { player ->
            val other: Player = getPartner(player) ?: return@interfaceClose
            if (player.hasRequest(other, "accept_trade")) {
                return@interfaceClose
            }
            reset(player, other)
            reset(other, player)
        }
    }

    /**
     * Requesting to trade with another player, accepting the request and setting up the trade
     * When an offer is updated the change is persisted to the other player
     */

    fun startTrade(player: Player, partner: Player) {
        reset(player, partner)
        player["other_trader_name"] = partner.name
        player["trade_partner"] = partner
        player.interfaces.apply {
            open("trade_main")
            close("inventory")
            open("trade_side")
            sendText("trade_main", "title", "Trading with: ${partner.name}")
            sendText("trade_main", "status", "")
        }
        player["offer_modified"] = false
        partner["other_offer_modified"] = false
        updateInventorySpaces(player, partner)
        player.interfaceOptions.apply {
            send("trade_main", "offer_options")
            unlockAll("trade_main", "offer_options", 0 until 28)

            send("trade_main", "other_options")
            unlockAll("trade_main", "other_options", 0 until 28)

            send("trade_side", "offer")
            unlockAll("trade_side", "offer", 0 until 28)

            unlockAll("trade_main", "loan_item")
            unlockAll("trade_main", "other_loan_item")
            unlockAll("trade_main", "loan_time")
        }
    }

    fun updateInventorySpaces(player: Player, other: Player) {
        player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
    }

    fun reset(player: Player, other: Player) {
        player.closeType("main_screen")
        player.closeType("underlay")
        player.interfaces.close("trade_side")
        player.interfaces.open("inventory")

        player.tab(Tab.Inventory)
        player["offer_value"] = 0
        player["other_offer_value"] = 0
        player["lend_time"] = 0

        player.clear("trade_partner")

        player.removeRequest(other, "trade")
        player.removeRequest(other, "accept_trade")
        player.removeRequest(other, "confirm_trade")

        player.offer.moveAll(player.inventory)
        player.offer.clear()
        player.otherOffer.clear()
        player.loan.moveAll(player.inventory)
        player.loan.clear()
        player.otherLoan.clear()
        player.sendScript("clear_dialogues")
    }
}

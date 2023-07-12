package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.clear
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.moveAll
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.entity.character.player.req.request
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.friend.friend
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.Trade.isTradeInterface
import world.gregs.voidps.world.interact.entity.player.display.Tab

/**
 * Requesting to trade with another player, accepting the request and setting up the trade
 * When an offer is updated the change is persisted to the other player
 */

on<PlayerOption>({ operate && option == "Trade with" }) { player: Player ->
    val filter = target["trade_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.friend(player))) {
        return@on
    }
    if (target.hasRequest(player, "trade")) {
        player.message("Sending trade offer...", ChatType.Trade)
    } else {
        player.message("Sending trade offer...", ChatType.Trade)
        target.message("wishes to trade with you.", ChatType.TradeRequest, name = player.name)
    }
    player.request(target, "trade") { requester, acceptor ->
        startTrade(requester, acceptor)
        startTrade(acceptor, requester)
    }
}

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

on<InterfaceClosed>({ isTradeInterface(id) }) { player: Player ->
    val other: Player = getPartner(player) ?: return@on
    if (player.hasRequest(other, "accept_trade")) {
        return@on
    }
    reset(player, other)
    reset(other, player)
}

fun updateInventorySpaces(player: Player, other: Player) {
    player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
}

fun reset(player: Player, other: Player) {
    player.closeType("main_screen")
    player.closeType("underlay")
    player.interfaces.close("trade_side")
    player.interfaces.open("inventory")

    player["tab"] = Tab.Inventory.name
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
}
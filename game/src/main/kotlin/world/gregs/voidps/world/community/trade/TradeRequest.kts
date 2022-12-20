package world.gregs.voidps.world.community.trade

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.clear
import world.gregs.voidps.engine.entity.character.contain.transact.moveAll
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.event.PlayerOption
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.community.friend.friend
import world.gregs.voidps.world.community.trade.lend.Loan.lendItem
import world.gregs.voidps.world.interact.entity.player.display.Tab

/**
 * Requesting to trade with another player, accepting the request and setting up the trade
 * When an offer is updated the change is persisted to the other player
 */

val logger = InlineLogger()

on<PlayerOption>({ option == "Trade with" }) { player: Player ->
    val filter = target["trade_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.friend(player))) {
        return@on
    }
    if (player.requests.has(target, "trade")) {
        player.message("Sending trade offer...", ChatType.Trade)
    } else {
        player.message("Sending trade offer...", ChatType.Trade)
        target.message("wishes to trade with you.", ChatType.TradeRequest, name = player.name)
    }
    target.requests.add(player, "trade") { requester, acceptor ->
        startTrade(requester, acceptor)
        startTrade(acceptor, requester)
    }
}

fun startTrade(player: Player, other: Player) {
    reset(player, other)
    player.setVar("other_trader_name", other.name)
    player["trade_partner"] = other

    player.action(ActionType.Trade) {
        try {
            sendMain(player, other)
            await<Unit>(Suspension.Infinite)
            tradeItems(player, other)
        } catch (e: CancellationException) {
            cancel(player)
            other.action.cancel(ActionType.Trade)
        } finally {
            reset(player, other)
            player.closeType("main_screen")
            player.closeType("underlay")
            player.interfaces.close("trade_side")
            player.interfaces.open("inventory")
        }
    }
}

on<ItemChanged>({ container == "inventory" && it.contains("trade_partner") }) { player: Player ->
    val other: Player = player["trade_partner"]
    updateInventorySpaces(other, player)
}


fun tradeItems(player: Player, other: Player) {
    if (!player.otherOffer.moveAll(player.inventory)) {
        logger.info { "Issue exchanging items $player ${player.otherOffer} ${player.otherLoan} ${player.inventory}" }
    }
    loanItem(player, other)
}

fun loanItem(player: Player, other: Player) {
    val loanItem = player.otherLoan.getItemId(0)
    val duration = other.getVar("lend_time", -1)
    if (loanItem.isBlank() || duration == -1) {
        return
    }
    lendItem(player, other, loanItem, duration)
}

fun updateInventorySpaces(player: Player, other: Player) {
    player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
}

fun cancel(player: Player) {
    player.offer.moveAll(player.inventory)
    player.loan.moveAll(player.inventory)
}

fun sendMain(player: Player, other: Player) {
    player.interfaces.apply {
        open("trade_main")
        close("inventory")
        open("trade_side")
        sendText("trade_main", "title", "Trading with: ${other.name}")
        sendText("trade_main", "status", "")
    }
    updateInventorySpaces(player, other)
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

fun reset(player: Player, other: Player) {
    player.setVar("tab", Tab.Inventory.name)
    player.setVar("offer_value", 0)
    player.setVar("other_offer_value", 0)
    player.setVar("lend_time", 0)

    player.clear("accepted_trade")
    player.clear("trade_partner")

    player.requests.remove(other, "trade")
    player.requests.remove(other, "accept_trade")
    player.requests.remove(other, "confirm_trade")

    player.loan.clear()
    player.otherLoan.clear()
    player.offer.clear()
    player.otherOffer.clear()
}

/*
    Loan
 */
on<ItemChanged>({ container == "item_loan" && it.contains("trade_partner") }) { player: Player ->
    val other: Player = player["trade_partner"]
    applyUpdates(other.otherLoan, this)
    val warn = player["accepted_trade", false] && removedAnyItems(this)
    modified(player, other, warn)
}


fun applyUpdates(container: Container, update: ItemChanged) {
    container.set(update.index, update.item.id, update.item.amount)
}

fun removedAnyItems(change: ItemChanged) = change.item.amount < change.oldItem.amount

fun modified(player: Player, other: Player, warned: Boolean) {
    if (warned) {
        player.setVar("offer_modified", true)
        other.setVar("other_offer_modified", true)
    }
    player.requests.remove(other, "accept_trade")
    player.interfaces.sendText("trade_main", "status", "")
}

/*
    Offer
 */
on<ItemChanged>({ container == "trade_offer" && it.contains("trade_partner") }) { player: Player ->
    val other: Player = player["trade_partner"]
    applyUpdates(other.otherOffer, this)
    val warn = player["accepted_trade", false] && removedAnyItems(this)
    if (warn) {
        highlightRemovedSlots(player, other, this)
    }
    modified(player, other, warn)
    updateValue(player, other)
}

fun highlightRemovedSlots(player: Player, other: Player, update: ItemChanged) {
    if (update.item.amount < update.oldItem.amount) {
        player.warn("trade_main", "offer_warning", update.index)
        other.warn("trade_main", "other_warning", update.index)
    }
}

val defs: InterfaceDefinitions by inject()
val containerDefinitions: ContainerDefinitions by inject()

fun Player.warn(id: String, component: String, slot: Int) {
    val comp = defs.get(id).getComponentOrNull(component) ?: return
    val container = containerDefinitions.get(comp["container", ""])
    sendScript(143, (comp["parent", -1] shl 16) or comp.id, container["width", 0.0], container["height", 0.0], slot)
}

fun updateValue(player: Player, other: Player) {
    val value = player.offer.calculateValue().toInt()
    player.setVar("offer_value", value)
    other.setVar("other_offer_value", value)
}
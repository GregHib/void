package world.gregs.voidps.world.community.trade

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.encode.sendScript
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.community.friend.hasFriend
import world.gregs.voidps.world.community.trade.lend.Loan.lendItem
import world.gregs.voidps.world.interact.entity.player.display.Tab

/**
 * Requesting to trade with another player, accepting the request and setting up the trade
 * When an offer is updated the change is persisted to the other player
 */

val logger = InlineLogger()

on<PlayerOption>({ option == "Trade with" }) { player: Player ->
    val filter = target["trade_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.hasFriend(player))) {
        return@on
    }
    if (player.requests.has(target, "trade")) {
        player.message("Sending trade offer...", ChatType.GameTrade)
    } else {
        player.message("Sending trade offer...", ChatType.GameTrade)
        target.message("wishes to trade with you.", ChatType.Trade, name = player.name)
    }
    target.requests.add(player, "trade") { requester, acceptor ->
        startTrade(requester, acceptor)
        startTrade(acceptor, requester)
    }
}

on<Command>({ prefix == "trade" }) { player: Player ->
    startTrade(player, player)
}

fun startTrade(player: Player, other: Player) {
    reset(player, other)
    player.setVar("other_trader_name", other.name)
    player["trade_partner"] = other

    val offerHandler: EventHandler = updateOffer(player, other)
    val loanHandler: EventHandler = updateLoan(player, other)
    val inventoryHandler: EventHandler = player.events.on<Player, ItemChanged>({ container == "inventory" }) {
        updateInventorySpaces(other, player)
    }

    player.action(ActionType.Trade) {
        try {
            sendMain(player, other)
            await<Unit>(Suspension.Infinite)
            tradeItems(player, other)
        } catch (e: CancellationException) {
            cancel(player)
            other.action.cancel(ActionType.Trade)
        } finally {
            player.events.remove(offerHandler)
            player.events.remove(loanHandler)
            player.events.remove(inventoryHandler)
            reset(player, other)
            player.closeType("main_screen")
            player.closeType("underlay")
            player.interfaces.close("trade_side")
            player.interfaces.open("inventory")
        }
    }
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

    player.loan.clearAll()
    player.otherLoan.clearAll()
    player.offer.clearAll()
    player.otherOffer.clearAll()
}

/*
    Loan
 */
fun updateLoan(player: Player, other: Player): EventHandler = player.events.on<Player, ItemChanged>({ container == "loan" }) { player: Player ->
    applyUpdates(other.otherLoan, this)
    val warn = player["accepted_trade", false] && removedAnyItems(this)
    modified(player, other, warn)
}

fun applyUpdates(container: Container, update: ItemChanged) {
    container.set(update.index, update.item.name, update.item.amount)
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
fun updateOffer(player: Player, other: Player): EventHandler = player.events.on<Player, ItemChanged>({ container == "offer" }) { update ->
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

fun Player.warn(name: String, component: String, slot: Int) {
    val defs: InterfaceDefinitions = world.gregs.voidps.utility.get()
    val containerDefinitions: ContainerDefinitions = world.gregs.voidps.utility.get()
    val comp = defs.get(name).getComponentOrNull(component) ?: return
    val container = containerDefinitions.get(comp["container", ""])
    sendScript(143, (comp["parent", -1] shl 16) or comp.id, container["width", 0.0], container["height", 0.0], slot)
}

fun updateValue(player: Player, other: Player) {
    val value = player.offer.calculateValue()
    player.setVar("offer_value", value)
    other.setVar("other_offer_value", value)
}
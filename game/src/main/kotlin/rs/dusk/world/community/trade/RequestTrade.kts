import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.Suspension
import rs.dusk.engine.action.action
import rs.dusk.engine.client.variable.BooleanVariable
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.contain.ContainerModification
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.set
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.command.Command
import rs.dusk.world.community.trade.*
import rs.dusk.world.community.trade.Trade.status
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * Requesting to trade with another player, accepting the request and setting up the trade
 */

val logger = InlineLogger()

BooleanVariable(1042, Variable.Type.VARP).register("offer_modified")
BooleanVariable(1043, Variable.Type.VARP).register("other_offer_modified")

IntVariable(729, Variable.Type.VARC).register("offer_value")
IntVariable(697, Variable.Type.VARC).register("other_offer_value")

IntVariable(203, Variable.Type.VARCSTR).register("other_trader_name")

PlayerOption where { option == "Trade with" } then {
    val filter = target["trade_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.hasFriend(player))) {
        return@then
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

Command where { prefix == "trade" } then {
//    val bus: EventBus = get()
//    val callback = { response: LoginResponse ->
//        if (response is LoginResponse.Success) {
//            val bot = response.player
//            bus.emit(PlayerRegistered(bot))
//            bus.emit(Registered(bot))
//            bot.viewport.loaded = true
//            startTrade(player, bot)
//        }
//    }
//    bus.emit(Login("Bot", callback = callback))
    startTrade(player, player)
}

fun startTrade(player: Player, other: Player) {
    reset(player)
    player.setVar("other_trader_name", other.name)
    player["trade_partner"] = other

    val offerListener: (List<ContainerModification>) -> Unit = { list ->
        val warn = player.requests.has(other, "trade_accept")
        for ((index, oldItem, oldAmount, item, amount) in list) {
            other.otherOffer.set(index, item, amount)
            if(warn) {
                player.warn("trade_main", "item_warning", index)
                other.warn("trade_main", "other_warning", index)
            }
        }
        if(warn) {
            player.setVar("offer_modified", true)
            other.setVar("other_offer_modified", true)
        }
        val value = calculateValue(player.offer.getItems(), player.offer.getAmounts())
        player.setVar("offer_value", value)
        other.setVar("other_offer_value", value)
        player.requests.remove(other, "trade_accept")
        status(player, "")
    }

    val loanListener: (List<ContainerModification>) -> Unit = { list ->
        val warn = player.requests.has(other, "trade_accept")
        for ((index, oldItem, oldAmount, item, amount) in list) {
            other.otherLoan.set(index, item, amount)
            if(warn) {
                player.warn("trade_main", "other_loan_item", index)
            }
        }
        player.requests.remove(other, "trade_accept")
        status(player, "")
    }

    val inventoryListener: (List<ContainerModification>) -> Unit = {
        updateInventorySpaces(other, player)
    }
    player.inventory.listeners.add(inventoryListener)
    player.offer.listeners.add(offerListener)
    player.loan.listeners.add(loanListener)

    player.action(ActionType.Trade) {
        try {
            status(other, "")
            sendMain(player, other)
            await(Suspension.Infinite)
        } catch (e: CancellationException) {
            cancel(player)
        } finally {
            player.inventory.listeners.remove(inventoryListener)
            player.offer.listeners.remove(offerListener)
            player.loan.listeners.remove(loanListener)
            reset(player)
            player.interfaces.close("trade_main")
            player.interfaces.close("trade_side")
        }
    }
}


fun updateInventorySpaces(player: Player, other: Player) {
    player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
}

val decoder: ItemDecoder by inject()

fun calculateValue(items: IntArray, amounts: IntArray): Long {
    var value = 0L
    for ((index, item) in items.withIndex()) {
        val amount = amounts[index]
        if (item != -1 && amount > 0) {
            val itemDef = decoder.get(item)
            value += (itemDef.cost * amount)
        }
    }
    return value
}

fun cancel(player: Player) {

}

fun sendMain(player: Player, other: Player) {
    player.interfaces.apply {
        open("trade_main")
        open("trade_side")
        sendText("trade_main", "title", "Trading with: ${other.name}")
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

InterfaceOption then {
    println(this)
}

fun reset(player: Player) {
    player.setVar("offer_value", 0)
    player.setVar("other_offer_value", 0)
    player.setVar("lend_time", 0)
    player.container("item_loan").clearAll()
    player.container("trade_offer").clearAll()
}

fun modified(player: Player) {
    player.setVar("offer_modified", true)
}

fun Player.hasFriend(other: Player) = true// TODO friends chat
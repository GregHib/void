import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.variable.BooleanVariable
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where

/**
 * Requesting to trade with another player, accepting the request and setting up the trade
 */

val logger = InlineLogger()

BooleanVariable(1042, Variable.Type.VARP).register("offer_modified")
BooleanVariable(1043, Variable.Type.VARP).register("other_offer_modified")
IntVariable(5026, Variable.Type.VARBIT).register("lend_time")
IntVariable(5070, Variable.Type.VARBIT).register("other_lend_time")

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
        println("Trade!")
    }
}

fun modified(player: Player) {
    player.setVar("other_trader_name", "Bob")
    player.setVar("offer_modified", true)
    player.setVar("offer_value", 12345)
    player.container("trade_item_loan").set(0, 11694)
    player.setVar("lend_time", 0)
}

fun unlockLend(player: Player) {
    player.interfaceOptions.unlockAll("trade_main", "loan_item")
    player.interfaceOptions.unlockAll("trade_main", "loan_time")
}

fun Player.hasFriend(other: Player) = true// TODO friends chat
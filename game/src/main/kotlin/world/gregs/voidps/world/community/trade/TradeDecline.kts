package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

/**
 * Declining or closing cancels the trade
 */

fun isTradeInterface(name: String) = name == "trade_main" || name == "trade_confirm"

fun isDecline(component: String, option: String) = component == "decline" && option == "Decline"

fun isClose(component: String, option: String) = component == "close" && option == "Close"

on<InterfaceOption>({ isTradeInterface(name) && (isDecline(component, option) || isClose(component, option)) }) { player: Player ->
    player.action.cancel(ActionType.Trade)
}
package content.social.trade.exchange

import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.inject

val exchange: GrandExchange by inject()
val definitions: ItemDefinitions by inject()

interfaceOpen("exchange_history") { player ->
    // TODO should be stored with player but what if player isn't online??
    /*val ids = exchange.history.history(player.accountName)
    for (i in ids.indices) {
        val id = ids[i]
        val offer = exchange.offers.offer(id)
        if (offer == null) {
            player.interfaces.sendText("exchange_history", "type_${i}", "")
            player.interfaces.sendText("exchange_history", "name_${i}", "")
            player.interfaces.sendText("exchange_history", "amount_${i}", "")
            player.interfaces.sendText("exchange_history", "price_${i}", "")
            continue
        }
        player.interfaces.sendText("exchange_history", "type_${i}", if (offer.sell) "You sold" else "You bought")
        player.interfaces.sendText("exchange_history", "name_${i}", definitions.get(offer.item).name)
        player.interfaces.sendText("exchange_history", "amount_${i}", offer.completed.toString())
        player.interfaces.sendText("exchange_history", "price_${i}", if (offer.sell) "You got<br>${offer.coins.toDigitGroupString()} gp" else "It cost you<br>${(offer.price * offer.completed).toDigitGroupString()} gp")
    }*/
}

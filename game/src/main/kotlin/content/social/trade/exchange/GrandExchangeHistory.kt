package content.social.trade.exchange

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.inject
import kotlin.math.absoluteValue

class GrandExchangeHistory : Script {

    val definitions: ItemDefinitions by inject()

    init {
        interfaceOpen("exchange_history") { player ->
            for (i in 0 until 5) {
                val history = player.history.getOrNull(i)
                if (history == null) {
                    player.interfaces.sendText("exchange_history", "type_$i", "")
                    player.interfaces.sendText("exchange_history", "name_$i", "")
                    player.interfaces.sendText("exchange_history", "amount_$i", "")
                    player.interfaces.sendText("exchange_history", "price_$i", "")
                    continue
                }
                val sell = history.coins < 0
                player.interfaces.sendText("exchange_history", "type_$i", if (sell) "You sold" else "You bought")
                player.interfaces.sendText("exchange_history", "name_$i", definitions.get(history.item).name)
                player.interfaces.sendText("exchange_history", "amount_$i", history.amount.toString())
                player.interfaces.sendText("exchange_history", "price_$i", "${if (sell) "You got" else "It cost you"}<br>${(history.coins.absoluteValue).toDigitGroupString()} gp")
            }
        }
    }
}

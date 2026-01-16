package content.social.trade.exchange

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import kotlin.math.absoluteValue

class GrandExchangeHistory(val definitions: ItemDefinitions) : Script {

    init {
        interfaceOpened("exchange_history") { id ->
            for (i in 0 until 5) {
                val history = history.getOrNull(i)
                if (history == null) {
                    interfaces.sendText(id, "type_$i", "")
                    interfaces.sendText(id, "name_$i", "")
                    interfaces.sendText(id, "amount_$i", "")
                    interfaces.sendText(id, "price_$i", "")
                    continue
                }
                val sell = history.coins < 0
                interfaces.sendText(id, "type_$i", if (sell) "You sold" else "You bought")
                interfaces.sendText(id, "name_$i", definitions.get(history.item).name)
                interfaces.sendText(id, "amount_$i", history.amount.toString())
                interfaces.sendText(id, "price_$i", "${if (sell) "You got" else "It cost you"}<br>${(history.coins.absoluteValue).toDigitGroupString()} gp")
            }
        }
    }
}

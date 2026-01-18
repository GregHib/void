package world.gregs.voidps.website.public

import world.gregs.voidps.engine.data.exchange.ExchangeHistory
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.engine.get

object GrandExchange {

    fun getTrackedItems(): List<String> {
        val history = get<ExchangeHistory>()
        return history.history.keys.sorted()
    }

    fun getItemName(id: String): String {
        val definitions = get<ItemDefinitions>()
        return definitions.get(id).name
    }

    fun getHistory(item: String): PriceHistory? {
        val history = get<ExchangeHistory>()
        return history.history[item]
    }
}

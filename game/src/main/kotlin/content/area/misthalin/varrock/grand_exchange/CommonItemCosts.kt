package content.area.misthalin.varrock.grand_exchange

import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions

class CommonItemCosts(
    val exchange: GrandExchange,
) : Script {

    init {
        interfaceOpened("common_item_costs") { id ->
            val type = get("common_item_costs", "ores")
            val enum = EnumDefinitions.get("exchange_items_$type")
            var index = 1
            for (i in 0 until enum.length) {
                val item = enum.getInt(i)
                val definition = ItemDefinitions.get(item)
                val price = exchange.history.marketPrice(definition.stringId)
                sendScript("send_common_item_price", index, i, "${price.toDigitGroupString()} gp")
                index += 2
            }
            interfaceOptions.unlockAll(id, "items", 0..enum.length * 2)
        }

        interfaceOption("Examine", "common_item_costs:items") { (item) ->
            message(item.def.getOrNull("examine") ?: return@interfaceOption)
        }
    }
}

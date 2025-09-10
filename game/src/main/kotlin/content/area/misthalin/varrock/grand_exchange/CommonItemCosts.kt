package content.area.misthalin.varrock.grand_exchange

import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.event.Script
@Script
class CommonItemCosts {

    val enums: EnumDefinitions by inject()
    val exchange: GrandExchange by inject()
    val itemDefinitions: ItemDefinitions by inject()
    
    init {
        interfaceOpen("common_item_costs") { player ->
            val type = player["common_item_costs", "ores"]
            val enum = enums.get("exchange_items_$type")
            var index = 1
            for (i in 0 until enum.length) {
                val item = enum.getInt(i)
                val definition = itemDefinitions.get(item)
                val price = exchange.history.marketPrice(definition.stringId)
                player.sendScript("send_common_item_price", index, i, "${price.toDigitGroupString()} gp")
                index += 2
            }
            player.interfaceOptions.unlockAll(id, "items", 0..enum.length * 2)
        }

        interfaceOption("Examine", "items", "common_item_costs") {
            player.message(item.def.getOrNull("examine") ?: return@interfaceOption)
        }

    }

}

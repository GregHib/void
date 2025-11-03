package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class GrandExchangeItemSets : Script {

    val enumDefinitions: EnumDefinitions by inject()
    val logger = InlineLogger()

    init {
        interfaceOpen("exchange_item_sets") { id ->
            open("exchange_sets_side")
            sendScript("grand_exchange_sets")
            interfaceOptions.unlockAll(id, "sets", 0..113)
        }

        interfaceClose("exchange_item_sets") { player ->
            player.close("exchange_sets_side")
        }

        interfaceOption("Components", "sets", "exchange_item_sets") {
            val descriptions = enumDefinitions.get("exchange_set_descriptions")
            player.message(descriptions.getString(item.def.id))
        }

        interfaceOption("Exchange", "sets", "exchange_item_sets") {
            val components: List<String> = item.def.getOrNull("items") ?: return@interfaceOption
            player.inventory.transaction {
                for (item in components) {
                    remove(item)
                }
                add(item.id)
            }
            when (player.inventory.transaction.error) {
                is TransactionError.Deficient -> {
                    //             https://youtu.be/Tz2jgdj1bWg?si=PQz8E4H2bPoBlAfA&t=94
                    player.message("You don't have the parts that make up this set.")
                }
                is TransactionError.Full -> player.inventoryFull()
                TransactionError.None -> {
                    //            https://youtu.be/FfVilurxzj0?si=wnz1ujXs_Xomfzmu&t=39
                    player.message("You successfully traded your item components for a set!")
                }
                TransactionError.Invalid -> logger.warn { "Invalid set exchange for item ${item.id} $components" }
            }
        }

        interfaceOption("Examine", "items", "exchange_item_sets") {
            player.message(item.def.getOrNull("examine") ?: return@interfaceOption)
        }

        interfaceOpen("exchange_sets_side") { id ->
            tab(Tab.Inventory)
            interfaceOptions.send(id, "items")
            interfaceOptions.unlockAll(id, "items", 0 until 28)
            sendInventory(inventory)
        }

        interfaceOption("Components", "items", "exchange_sets_side") {
            val descriptions = enumDefinitions.get("exchange_set_descriptions")
            val text = descriptions.getString(item.def.id)
            if (text != "shop_dummy") {
                player.message(text)
            } else {
                player.message("That isn't a set item.")
            }
        }

        interfaceOption("Exchange", "items", "exchange_sets_side") {
            exchangeSet(player, item, itemSlot)
        }

        interfaceOption("Examine", "items", "exchange_sets_side") {
            player.message(item.def.getOrNull("examine") ?: return@interfaceOption)
        }

        itemOnNPCApproach(npc = "grand_exchange_clerk*") {
            approachRange(2)
            exchangeSet(this, it.item, it.slot)
        }
    }

    /*
        Side
     */

    fun exchangeSet(player: Player, item: Item, slot: Int) {
        val components: List<String>? = item.def.getOrNull("items")
        if (components == null) {
            player.message("That isn't a set item, you can't break it up into component parts.")
            return
        }
        player.inventory.transaction {
            remove(slot, item.id)
            for (component in components) {
                add(component)
            }
        }
        when (player.inventory.transaction.error) {
            is TransactionError.Full -> player.inventoryFull("for the component parts")
            TransactionError.None -> {
                //            https://youtu.be/FfVilurxzj0?si=wnz1ujXs_Xomfzmu&t=39
                player.message("You successfully traded your set for its component items!")
            }
            else -> logger.warn { "${player.inventory.transaction.error} set exchange for item ${item.id} $components" }
        }
    }
}

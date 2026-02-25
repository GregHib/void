package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class GrandExchangeItemSets : Script {

    val logger = InlineLogger()

    init {
        interfaceOpened("exchange_item_sets") { id ->
            open("exchange_sets_side")
            sendScript("grand_exchange_sets")
            interfaceOptions.unlockAll(id, "sets", 0..113)
        }

        interfaceClosed("exchange_item_sets") {
            close("exchange_sets_side")
        }

        interfaceOption("Components", "exchange_item_sets:sets") { (item) ->
            val descriptions = EnumDefinitions.get("exchange_set_descriptions")
            message(descriptions.getString(item.def.id))
        }

        interfaceOption("Exchange", "exchange_item_sets:sets") { (item) ->
            val components: List<String> = item.def.getOrNull("items") ?: return@interfaceOption
            inventory.transaction {
                for (item in components) {
                    remove(item)
                }
                add(item.id)
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> {
                    //             https://youtu.be/Tz2jgdj1bWg?si=PQz8E4H2bPoBlAfA&t=94
                    message("You don't have the parts that make up this set.")
                }
                is TransactionError.Full -> inventoryFull()
                TransactionError.None -> {
                    //            https://youtu.be/FfVilurxzj0?si=wnz1ujXs_Xomfzmu&t=39
                    message("You successfully traded your item components for a set!")
                }
                TransactionError.Invalid -> logger.warn { "Invalid set exchange for item ${item.id} $components" }
            }
        }

        interfaceOption("Examine", "exchange_item_sets:items") { (item) ->
            message(item.def.getOrNull("examine") ?: return@interfaceOption)
        }

        interfaceOpened("exchange_sets_side") { id ->
            tab(Tab.Inventory)
            interfaceOptions.send(id, "items")
            interfaceOptions.unlockAll(id, "items", 0 until 28)
            sendInventory(inventory)
        }

        interfaceOption("Components", "exchange_sets_side:items") { (item) ->
            val descriptions = EnumDefinitions.get("exchange_set_descriptions")
            val text = descriptions.getString(item.def.id)
            if (text != "shop_dummy") {
                message(text)
            } else {
                message("That isn't a set item.")
            }
        }

        interfaceOption("Exchange", "exchange_sets_side:items") { (item, itemSlot) ->
            exchangeSet(this, item, itemSlot)
        }

        interfaceOption("Examine", "exchange_sets_side:items") { (item) ->
            message(item.def.getOrNull("examine") ?: return@interfaceOption)
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

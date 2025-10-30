package content.entity.npc.shop

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.general.GeneralStores
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.engine.inv.sendInventory

class ShopOpen : Script {

    val itemDefinitions: ItemDefinitions by inject()
    val inventoryDefinitions: InventoryDefinitions by inject()
    val logger = InlineLogger()

    init {
        npcOperate("Trade") {
            if (def.contains("shop")) {
                target.face(player)
                player.openShop(def["shop"])
            }
        }

        interfaceClose("shop") { player ->
            player.close("item_info")
            player.close("shop_side")
            val shop = player.shop()
            if (shop.endsWith("general_store")) {
                GeneralStores.unbind(player, shop)
            }
        }

        shopOpen { player ->
            val definition = inventoryDefinitions.getOrNull(id) ?: return@shopOpen
            val currency: String = definition["currency", "coins"]
            player["shop_currency"] = currency
            player["item_info_currency"] = currency
            player["shop"] = id
            player.interfaces.open("shop")
            player.open("shop_side")
            val inventorySample = "${id}_sample"

            player["free_inventory"] = inventoryDefinitions.get(inventorySample).id
            val sample = openShopInventory(player, inventorySample)
            player.interfaceOptions.unlockAll("shop", "sample", 0 until sample.size * 5)

            player["main_inventory"] = definition.id
            val main = openShopInventory(player, id)
            sendAmounts(player, main)
            player.interfaceOptions.unlockAll("shop", "stock", 0 until main.size * 6)

            player.interfaces.sendVisibility("shop", "store", id.endsWith("general_store"))
            player.interfaces.sendText("shop", "title", definition["title", definition.stringId.toTitleCase()])
        }

        interfaceRefresh("shop_side") { player ->
            player.interfaceOptions.send("shop_side", "inventory")
            player.interfaceOptions.unlockAll("shop_side", "inventory", 0 until 28)
        }

        inventoryChanged { player ->
            if (player.contains("shop") && player["shop", ""] == inventory) {
                player["amount_$index"] = item.amount
            }
        }
    }

    fun openShopInventory(player: Player, id: String): Inventory = if (id.endsWith("general_store")) {
        GeneralStores.bind(player, id)
    } else {
        val new = !player.inventories.contains(id)
        val inventory = player.inventories.inventory(id)
        if (new) {
            fillShop(inventory, id)
        }
        player.sendInventory(id)
        inventory
    }

    fun fillShop(inventory: Inventory, shopId: String) {
        val definition = inventoryDefinitions.get(shopId)
        if (!definition.contains("shop")) {
            logger.warn { "Invalid shop definition $shopId" }
        }
        for (index in 0 until definition.length) {
            val id = definition.ids?.getOrNull(index) ?: continue
            val amount = definition.amounts?.getOrNull(index) ?: continue
            val itemDefinition = itemDefinitions.get(id)
            inventory.transaction { set(index, Item(itemDefinition.stringId, amount)) }
        }
    }

    fun sendAmounts(player: Player, inventory: Inventory) {
        for ((index, item) in inventory.items.withIndex()) {
            player["amount_$index"] = item.amount
        }
    }
}

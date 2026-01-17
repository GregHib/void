package content.entity.npc.shop

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.general.GeneralStores
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.sendInventory

class ShopOpen(
    val itemDefinitions: ItemDefinitions,
    val inventoryDefinitions: InventoryDefinitions,
) : Script {

    val logger = InlineLogger()

    init {
        npcOperate("Trade") { (target) ->
            val def = target.def(this)
            if (def.contains("shop")) {
                target.face(this)
                openShop(def["shop"])
            }
        }

        interfaceClosed("shop") {
            close("item_info")
            close("shop_side")
            val shop = shop()
            if (shop.endsWith("general_store")) {
                GeneralStores.unbind(this, shop)
            }
        }

        shopOpen { id ->
            val definition = inventoryDefinitions.getOrNull(id) ?: return@shopOpen
            val currency: String = definition["currency", "coins"]
            set("shop_currency", currency)
            set("item_info_currency", currency)
            set("shop", id)
            interfaces.open("shop")
            open("shop_side")
            val inventorySample = "${id}_sample"

            set("free_inventory", inventoryDefinitions.get(inventorySample).id)
            val sample = openShopInventory(this, inventorySample)
            interfaceOptions.unlockAll("shop", "sample", 0 until sample.size * 5)

            set("main_inventory", definition.id)
            val main = openShopInventory(this, id)
            sendAmounts(this, main)
            interfaceOptions.unlockAll("shop", "stock", 0 until main.size * 6)

            interfaces.sendVisibility("shop", "store", id.endsWith("general_store"))
            interfaces.sendText("shop", "title", definition["title", definition.stringId.toTitleCase()])
        }

        interfaceRefresh("shop_side") {
            interfaceOptions.send("shop_side", "inventory")
            interfaceOptions.unlockAll("shop_side", "inventory", 0 until 28)
        }

        slotChanged { (inventory, index, item) ->
            if (contains("shop") && get("shop", "") == inventory) {
                set("amount_$index", item.amount)
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

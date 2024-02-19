package world.gregs.voidps.world.interact.entity.npc.shop

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.engine.inv.sendInventory

val inventoryDefinitions: InventoryDefinitions by inject()
val logger = InlineLogger()

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

on<OpenShop> { player: Player ->
    val definition = inventoryDefinitions.getOrNull(id) ?: return@on
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
    player.interfaces.sendText("shop", "title", definition["title", "Shop"])
}

interfaceRefresh("shop_side") { player ->
    player.interfaceOptions.send("shop_side", "inventory")
    player.interfaceOptions.unlockAll("shop_side", "inventory", 0 until 28)
}

fun openShopInventory(player: Player, id: String): Inventory {
    return if (id.endsWith("general_store")) {
        GeneralStores.bind(player, id)
    } else {
        val new = !player.inventories.containsKey(id)
        val inventory = player.inventories.inventory(id)
        if (new) {
            fillShop(inventory, id)
        }
        player.sendInventory(id)
        inventory
    }
}

fun fillShop(inventory: Inventory, shopId: String) {
    val def = inventoryDefinitions.get(shopId)
    if (!def.contains("shop")) {
        logger.warn { "Invalid shop definition $shopId" }
    }
    val list = def.getOrNull<List<Map<String, Int>>>("defaults") ?: return
    for (index in 0 until def.length) {
        val map = list.getOrNull(index) ?: continue
        val id = map.keys.firstOrNull() ?: continue
        val amount = map.values.firstOrNull() ?: 0
        inventory.transaction { set(index, Item(id, amount)) }
    }
}

itemChange { player ->
    if (player.contains("shop") && player["shop", ""] == inventory) {
        player["amount_${index}"] = item.amount
    }
}

fun sendAmounts(player: Player, inventory: Inventory) {
    for ((index, item) in inventory.items.withIndex()) {
        player["amount_$index"] = item.amount
    }
}

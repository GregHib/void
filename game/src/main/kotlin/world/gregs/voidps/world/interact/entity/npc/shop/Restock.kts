package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

/**
 * Every [restockTimeTicks] all players shops and [GeneralStores] update their stock by 10%
 */
val inventoryDefinitions: InventoryDefinitions by inject()
val restockTimeTicks = TimeUnit.SECONDS.toTicks(60)

playerSpawn { player ->
    player.softTimers.restart("shop_restock")
}

timerStart("shop_restock") { _ ->
    interval = restockTimeTicks
}

timerTick("shop_restock") { player ->
    for (name in player.inventories.keys) {
        val inventory = player.inventories.inventory(name)
        val def = inventoryDefinitions.get(name)
        if (!def["shop", false]) {
            continue
        }
        restock(def, inventory)
    }
}

// Remove restocked shops to save space
playerDespawn { player ->
    for ((name, inventory) in player.inventories.instances) {
        val def = inventoryDefinitions.get(name)
        if (!def["shop", false]) {
            continue
        }
        val amounts = def.amounts ?: continue
        if (inventory.items.withIndex().all { (index, item) -> item.amount == amounts.getOrNull(index) }) {
            player.inventories.remove(name)
        }
    }
}

worldSpawn {
    restock()
}

fun restock() {
    World.run("general_store_restock", restockTimeTicks) {
        for ((key, inventory) in GeneralStores.stores) {
            val def = inventoryDefinitions.get(key)
            restock(def, inventory)
        }
        restock()
    }
}

fun restock(def: InventoryDefinition, inventory: Inventory) {
    val defaults = def.getOrNull<List<Map<String, Int>>>("defaults")
    for (index in 0 until def.length) {
        val map = defaults?.getOrNull(index)
        var maximum = map?.values?.firstOrNull()
        val id = map?.keys?.firstOrNull()
        val item = inventory[index]
        if (id == null || maximum == null) {
            maximum = 0
        }
        if (maximum == item.amount) {
            continue
        }
        val difference = abs(item.amount - maximum)
        val percent = max(1, (difference * 0.1).toInt())
        if (item.amount < maximum) {
            inventory.add(item.id, percent)
        } else {
            inventory.remove(item.id, percent)
        }
    }
}
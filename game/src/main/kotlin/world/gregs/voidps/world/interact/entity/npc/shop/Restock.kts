package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

/**
 * Every [restockTimeTicks] all players shops and [GeneralStores] update their stock by 10%
 */
val inventoryDefinitions: InventoryDefinitions by inject()
val restockTimeTicks = TimeUnit.SECONDS.toTicks(60)

on<Registered> { player: Player ->
    player.softTimers.restart("shop_restock")
}

on<TimerStart>({ timer == "shop_restock" }) { _: Player ->
    interval = restockTimeTicks
}

on<TimerTick>({ timer == "shop_restock" }) { player: Player ->
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
on<Unregistered> { player: Player ->
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

on<World, Registered> {
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

fun restock(def: InventoryDefinition, inventory: world.gregs.voidps.engine.inv.Inventory) {
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
package content.entity.npc.shop.stock

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.general.GeneralStores
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

class Restock : Script {

    val inventoryDefinitions: InventoryDefinitions by inject()
    val logger = InlineLogger()

    init {
        playerSpawn {
            softTimers.restart("shop_restock")
        }

        worldSpawn {
            World.timers.start("general_store_restock")
        }

        timerStart("shop_restock") { TimeUnit.SECONDS.toTicks(60) }
        timerTick("shop_restock", ::restockShops)
        worldTimerStart("general_store_restock") { TimeUnit.SECONDS.toTicks(60) }
        worldTimerTick("general_store_restock", ::restockGeneralStores)

        playerDespawn { player ->
            val removal = mutableListOf<String>()
            for ((name, inventory) in player.inventories.instances) {
                val def = inventoryDefinitions.get(name)
                if (!def["shop", false]) {
                    continue
                }
                val amounts = def.amounts ?: continue
                if (inventory.items.withIndex().all { (index, item) -> item.amount == amounts.getOrNull(index) }) {
                    removal.add(name)
                }
            }
            for (name in removal) {
                player.inventories.instances.remove(name)
            }
        }
    }

    fun restockShops(player: Player): Int {
        for ((name, inventory) in player.inventories.instances) {
            val def = inventoryDefinitions.get(name)
            if (!def["shop", false]) {
                continue
            }
            restock(def, inventory)
        }
        return Timer.CONTINUE
    }

    /**
     * Every 60 seconds update stock of all players shops and [GeneralStores] by 10%
     */
    fun restockGeneralStores(): Int {
        logger.debug { "Restocking general stores." }
        for ((key, inventory) in GeneralStores.stores) {
            val def = inventoryDefinitions.get(key)
            restock(def, inventory)
        }
        return Timer.CONTINUE
    }

    // Remove restocked shops to save space
    fun restock(def: InventoryDefinition, inventory: Inventory) {
        for (index in 0 until def.length) {
            val id = def.ids?.getOrNull(index)
            var maximum = def.amounts?.getOrNull(index)
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
}

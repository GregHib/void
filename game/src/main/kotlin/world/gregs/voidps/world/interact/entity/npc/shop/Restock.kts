import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.utility.inject
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.npc.shop.GeneralStores
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

/**
 * Every [restockTimeTicks] all players shops and [GeneralStores] update their stock by 10%
 */
val containerDefs: ContainerDefinitions by inject()
val itemDefs: ItemDefinitions by inject()
val restockTimeTicks = TimeUnit.SECONDS.toTicks(60)

on<Registered> { player: Player ->
    delay(player, restockTimeTicks, loop = true) {
        for (name in player.containers.keys) {
            val container = player.container(name)
            val def = containerDefs.get(name)
            if (!def["shop", false]) {
                continue
            }
            restock(def, container)
        }
    }
}

on<World, Startup> {
    delay(restockTimeTicks, loop = true) {
        for ((key, container) in GeneralStores.stores) {
            val def = containerDefs.get(key)
            restock(def, container)
        }
    }
}

fun restock(def: ContainerDefinition, container: Container) {
    for (index in 0 until def.length) {
        var maximum = def.amounts?.getOrNull(index)
        val id = def.ids?.getOrNull(index)
        val item = container.getItem(index)
        if (id == null || maximum == null) {
            maximum = 0
        }
        if (maximum == item.amount) {
            continue
        }
        val difference = abs(item.amount - maximum)
        val percent = max(1, (difference * 0.1).toInt())
        if (item.amount < maximum) {
            container.add(index, item.name, percent)
        } else {
            container.remove(index, item.name, percent)
        }
    }
}
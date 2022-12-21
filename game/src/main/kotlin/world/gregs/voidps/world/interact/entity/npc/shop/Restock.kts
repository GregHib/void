import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.remove
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.interact.entity.npc.shop.GeneralStores
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

/**
 * Every [restockTimeTicks] all players shops and [GeneralStores] update their stock by 10%
 */
val containerDefs: ContainerDefinitions by inject()
val restockTimeTicks = TimeUnit.SECONDS.toTicks(60)

on<Registered> { player: Player ->
    player.delay(restockTimeTicks, loop = true) {
        for (name in player.containers.keys) {
            val container = player.containers.container(name)
            val def = containerDefs.get(name)
            if (!def["shop", false]) {
                continue
            }
            restock(def, container)
        }
    }
}

// Remove restocked shops to save space
on<Unregistered> { player: Player ->
    for ((name, container) in player.containers.instances) {
        val def = containerDefs.get(name)
        if (!def["shop", false]) {
            continue
        }
        val amounts = def.amounts ?: continue
        if (container.items.withIndex().all { (index, item) -> item.amount == amounts.getOrNull(index) }) {
            player.containers.remove(name)
        }
    }
}

on<World, Registered> { world ->
    world.delay(restockTimeTicks, loop = true) {
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
        val item = container[index]
        if (id == null || maximum == null) {
            maximum = 0
        }
        if (maximum == item.amount) {
            continue
        }
        val difference = abs(item.amount - maximum)
        val percent = max(1, (difference * 0.1).toInt())
        if (item.amount < maximum) {
            container.add(item.id, percent)
        } else {
            container.remove(item.id, percent)
        }
    }
}
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.utility.toTicks
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

val containerDefs: ContainerDefinitions by inject()
val restockTimeTicks = TimeUnit.SECONDS.toTicks(10)

/**
 * Every [restockTimeTicks] all players shops restock by 10%
 */
on<Registered> { player: Player ->
    delay(player, restockTimeTicks, loop = true) {
        for (name in player.containers.keys) {
            val container = player.container(name)
            val def = containerDefs.get(name)
            if (!def["shop", false]) {
                continue
            }
            for (index in 0 until def.length) {
                val maximum = def.amounts?.getOrNull(index) ?: continue
                val current = container.getAmount(index)
                if (maximum == current) {
                    continue
                }
                val item = container.getItem(index)
                val difference = abs(current - maximum)
                val percent = max(1, (difference * 0.1).toInt())
                if (current < maximum) {
                    container.add(item, percent)
                } else {
                    container.remove(item, percent)
                }
            }
        }
    }
}
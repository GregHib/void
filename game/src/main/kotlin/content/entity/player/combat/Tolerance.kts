package content.entity.player.combat

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit

/**
 * Certain NPCs stop being aggressive towards the player if they stay inside their tolerance area for [toleranceTime]
 */
val toleranceTime = TimeUnit.MINUTES.toSeconds(10)

playerSpawn { player ->
    if (!player.contains("tolerance")) {
        player.start("tolerance", toleranceTime.toInt(), epochSeconds())
    }
    player["tolerance_area"] = player.tile.toCuboid(10)
}

move({ to !in it.getOrPut("tolerance_area") { player.tile.toCuboid(10) } }) { player ->
    player["tolerance_area"] = player.tile.toCuboid(10)
    player.start("tolerance", toleranceTime.toInt(), epochSeconds())
}

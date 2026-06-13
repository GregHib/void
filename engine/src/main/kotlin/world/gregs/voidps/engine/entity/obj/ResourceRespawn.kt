package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.player.Players
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Player-count-scaled respawn timing for gathering resources (trees, rocks).
 * Respawn seconds = base * 0.6 + factor * sqrt(base), where factor scales
 * from 7 at low population down to 1 at high population.
 *
 * Based on the RuneHQ mining and woodcutting respawn charts.
 * https://www.runehq.com/skill/mining#spawntimes
 * https://www.runehq.com/skill/woodcutting#respawntimes
 */
object ResourceRespawn {

    /**
     * @param base respawn time in ticks at maximum population
     * @param players current world player count
     * @return respawn time in ticks (1 tick = 600ms)
     */
    fun ticks(base: Int, players: Int = Players.size): Int {
        if (players >= MAX_PLAYERS - 100) {
            return base.coerceAtLeast(1)
        }
        val steps = players / 250
        val factor = 8 - if (steps == 0) 1 else steps
        return (base + factor * sqrt(base.toDouble()) / 0.6).roundToInt().coerceAtLeast(1)
    }
}

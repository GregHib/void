package world.gregs.voidps.engine.client.update.task.player

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * Calculates paths for players that want to move
 */
class PlayerPathTask(override val entities: Players, val finder: PathFinder) : EntityTask<Player>() {

    private val logger = InlineLogger()

    override fun predicate(entity: Player): Boolean {
        return entity.movement.strategy != null
    }

    override fun runAsync(player: Player) {
        val strategy = player.movement.strategy!!
        player.movement.result = finder.find(player, strategy)
        player.movement.length = player.movement.steps.size
        player.movement.strategy = null
        logger.debug { "Path length: ${player.movement.length}" }
    }

}
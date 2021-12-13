package world.gregs.voidps.engine.client.update.task.player

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.move.Path
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
        return entity.movement.path.state == Path.State.Waiting
    }

    override fun runAsync(player: Player) {
        val path = player.movement.path
        path.result = finder.find(player, path, path.smart)
        if (player.client != null) {
            logger.debug { "Path length: ${path.steps.size}" }
        }
    }

}
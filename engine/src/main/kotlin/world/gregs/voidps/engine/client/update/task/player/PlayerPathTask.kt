package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * Calculates paths for players that want to move
 */
class PlayerPathTask(override val entities: Players, val finder: PathFinder) : EntityTask<Player>() {

    override fun predicate(entity: Player): Boolean {
        return entity.movement.target
    }

    override fun runAsync(player: Player) {
        val strategy = player.movement.strategy!!
        val callback = player.movement.completable!!
        player.movement.target = false
        val result = finder.find(player, strategy)
        player.movement.callback = {
            callback.complete(result)
        }
    }

}
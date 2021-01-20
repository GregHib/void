package world.gregs.void.engine.client.update.task.player

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.Players
import world.gregs.void.engine.event.Priority.PLAYER_UPDATE_FINISHED
import world.gregs.void.engine.tick.task.EntityTask

/**
 * Resets non-persistent changes
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerPostUpdateTask(override val entities: Players) : EntityTask<Player>(PLAYER_UPDATE_FINISHED) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) {
        player.viewport.shift()
        player.viewport.players.update()
        player.viewport.npcs.update()
        player.movement.reset()
        player.visuals.aspects.forEach { (_, visual) ->
            visual.reset(player)
        }
    }

}
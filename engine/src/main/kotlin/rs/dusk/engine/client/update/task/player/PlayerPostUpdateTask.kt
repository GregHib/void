package rs.dusk.engine.client.update.task.player

import rs.dusk.engine.event.Priority.PLAYER_UPDATE_FINISHED
import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.Players

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
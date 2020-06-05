package rs.dusk.engine.client.update.task

import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.Players

/**
 * Resets non-persistent changes
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerPostUpdateTask(override val entities: Players) : EntityTask<Player>() {

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
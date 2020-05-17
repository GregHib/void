package rs.dusk.engine.task

import rs.dusk.engine.EntityTask
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.Players

/**
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
            visual.reset()
        }
    }

}
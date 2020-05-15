package rs.dusk.engine.task

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.EntityTask
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.visual.getAnimation
import rs.dusk.engine.model.entity.index.update.visual.getGraphic
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerPostUpdateTask(override val entities: Players) : EntityTask<Player>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) = GlobalScope.async<Unit> {
        player.viewport.shift()
        player.viewport.players.update()
        player.viewport.npcs.update()
        val lastSeen = player.viewport.players.lastSeen[player] ?: Tile.EMPTY
        val region = player.movement.lastTile.delta(lastSeen).regionPlane
        val regionChanged = region.x != 0 || region.y != 0 || region.plane != 0
        if (regionChanged) {
            player.viewport.players.lastSeen[player] = player.tile
        }
        player.movement.delta = Tile(0)// Post movement not updating?
        player.movement.walkStep = Direction.NONE
        player.movement.runStep = Direction.NONE
        player.getAnimation().apply {
            first = -1
            second = -1
            third = -1
            fourth = -1
            speed = 0
        }
        repeat(4) {
            player.getGraphic(it).apply {
                id = -1
                delay = 0
                height = 0
                rotation = 0
                forceRefresh = false
            }
        }
    }

}
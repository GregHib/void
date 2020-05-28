package rs.dusk.engine.client.update.task

import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.index.Move
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerPostUpdateTask(override val entities: Players, private val bus: EventBus) : EntityTask<Player>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) {
        player.viewport.shift()
        player.viewport.players.update()
        player.viewport.npcs.update()
        if (player.movement.delta != Tile.EMPTY) {
            player.movement.lastTile = player.tile
            entities.remove(player.tile, player)
            entities.remove(player.tile.chunk, player)
            player.tile = player.tile.add(player.movement.delta)
            entities.add(player.tile, player)
            entities.add(player.tile.chunk, player)
            bus.emit(Move(player, player.movement.lastTile, player.tile))
        }
        player.movement.reset()
        player.visuals.aspects.forEach { (_, visual) ->
            visual.reset()
        }
    }

}
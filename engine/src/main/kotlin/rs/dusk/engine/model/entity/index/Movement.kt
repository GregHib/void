package rs.dusk.engine.model.entity.index

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
data class Movement(
    var lastTile: Tile = Tile(0),
    var delta: Tile = Tile(0),
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE
)

val NPC.teleport: Boolean
    get() = movement.delta.id != 0 && movement.walkStep == Direction.NONE && movement.runStep == Direction.NONE
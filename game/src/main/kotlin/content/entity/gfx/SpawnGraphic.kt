package content.entity.gfx

import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.zone.GraphicAddition
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

/**
 * Play graphic [id] at [tile], [height] client units off the ground (one tile is 512 wide).
 * The packet carries the height as a single byte so anything higher is capped at [MAX_AREA_GFX_HEIGHT].
 */
fun areaGfx(
    id: String,
    tile: Tile,
    delay: Int = 0,
    height: Int = 0,
    rotation: Direction = Direction.SOUTH,
) {
    ZoneBatchUpdates.add(tile.zone, GraphicAddition(tile.id, GraphicDefinitions.get(id).id, height.coerceIn(0, MAX_AREA_GFX_HEIGHT), delay, rotation.ordinal))
}

const val MAX_AREA_GFX_HEIGHT = 255

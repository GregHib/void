package world.gregs.voidps.world.interact.entity.gfx

import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.zone.GraphicAddition
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

fun areaGraphic(
    id: String,
    tile: Tile,
    delay: Int = 0,
    height: Int = 0,
    rotation: Direction = Direction.SOUTH
) {
    val batches: ZoneBatchUpdates by inject()
    val definitions: GraphicDefinitions by inject()
    batches.add(tile.zone, GraphicAddition(tile.id, definitions.get(id).id, height, delay, rotation.ordinal))
}
package world.gregs.voidps.world.interact.entity.gfx

import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.encode.chunk.GraphicAddition

fun areaGraphic(
    id: String,
    tile: Tile,
    delay: Int = 0,
    height: Int = 0,
    rotation: Direction = Direction.SOUTH
) {
    val batches: ChunkBatchUpdates by inject()
    val definitions: GraphicDefinitions by inject()
    batches.add(tile.chunk, GraphicAddition(tile.id, definitions.get(id).id, height, delay, rotation.ordinal))
}
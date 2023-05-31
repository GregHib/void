package world.gregs.voidps.world.interact.entity.gfx

import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.GraphicDefinitions
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.item.floor.offset
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.encode.chunk.GraphicAddition

fun areaGraphic(
    id: String,
    tile: Tile,
    delay: Int = 0,
    height: Int = 0,
    rotation: Direction = Direction.SOUTH,
    owner: String? = null
) {
    val batches: ChunkBatchUpdates by inject()
    val definitions: GraphicDefinitions by inject()
    batches.add(tile.chunk, GraphicAddition(definitions.get(id).id, tile.offset(), height, delay, rotation.ordinal, owner))
}
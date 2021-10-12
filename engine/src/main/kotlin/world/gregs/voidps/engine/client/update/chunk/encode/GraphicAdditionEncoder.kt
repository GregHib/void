package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.GraphicAddition
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol

class GraphicAdditionEncoder : ChunkEncoder<GraphicAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: GraphicAddition) = writer.run {
        writeByte(Protocol.Batch.GRAPHIC_AREA)
        writeByte(update.areaGraphic.tile.offset())
        writeShort(update.areaGraphic.graphic.id)
        writeByte(update.areaGraphic.graphic.height)
        writeShort(update.areaGraphic.graphic.delay)
        writeByte(update.areaGraphic.graphic.rotation)
    }
}
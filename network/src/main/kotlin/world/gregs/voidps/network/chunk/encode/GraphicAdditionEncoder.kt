package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.GraphicAddition

class GraphicAdditionEncoder : ChunkEncoder<GraphicAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: GraphicAddition) = writer.run {
        writeByte(Protocol.Batch.GRAPHIC_AREA)
        writeByte(update.tileOffset)
        writeShort(update.id)
        writeByte(update.height)
        writeShort(update.delay)
        writeByte(update.rotation)
    }
}
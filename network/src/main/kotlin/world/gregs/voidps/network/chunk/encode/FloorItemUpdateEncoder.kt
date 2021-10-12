package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.FloorItemUpdate

class FloorItemUpdateEncoder : ChunkEncoder<FloorItemUpdate> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemUpdate) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_UPDATE)
        writeByte(update.tileOffset)
        writeShort(update.id)
        writeShort(update.stack)
        writeShort(update.combined)
    }
}
package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.FloorItemRemoval
import world.gregs.voidps.network.writeByteSubtract
import world.gregs.voidps.network.writeShortAddLittle

class FloorItemRemovalEncoder : ChunkEncoder<FloorItemRemoval> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemRemoval) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_REMOVE)
        writeShortAddLittle(update.id)
        writeByteSubtract(update.tileOffset)
    }
}
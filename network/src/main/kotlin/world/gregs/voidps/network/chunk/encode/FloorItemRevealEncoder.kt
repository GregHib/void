package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.FloorItemReveal
import world.gregs.voidps.network.writeShortAdd
import world.gregs.voidps.network.writeShortLittle

class FloorItemRevealEncoder : ChunkEncoder<FloorItemReveal> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemReveal) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_REVEAL)
        writeShortLittle(update.amount)
        writeByte(update.tileOffset)
        writeShortAdd(update.id)
        writeShortAdd(update.ownerIndex)
    }
}
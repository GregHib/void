package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.FloorItemAddition
import world.gregs.voidps.network.writeShortLittle

class FloorItemAdditionEncoder : ChunkEncoder<FloorItemAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemAddition) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_ADD)
        writeShortLittle(update.amount)
        writeShortLittle(update.id)
        writeByte(update.tileOffset)
    }
}
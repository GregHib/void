package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.FloorItemReveal
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.writeShortAdd
import world.gregs.voidps.network.writeShortLittle

class FloorItemRevealEncoder : ChunkEncoder<FloorItemReveal> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemReveal) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_REVEAL)
        writeShortLittle(update.floorItem.amount)
        writeByte(update.floorItem.tile.offset())
        writeShortAdd(update.floorItem.id)
        writeShortAdd(update.owner)
    }
}
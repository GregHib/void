package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.FloorItemRemoval
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.writeByteSubtract
import world.gregs.voidps.network.writeShortAddLittle

class FloorItemRemovalEncoder : ChunkEncoder<FloorItemRemoval> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemRemoval) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_REMOVE)
        writeShortAddLittle(update.floorItem.id)
        writeByteSubtract(update.floorItem.tile.offset())
    }
}
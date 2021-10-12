package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.FloorItemUpdate
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol

class FloorItemUpdateEncoder : ChunkEncoder<FloorItemUpdate> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemUpdate) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_UPDATE)
        writeByte(update.floorItem.tile.offset())
        writeShort(update.floorItem.id)
        writeShort(update.stack)
        writeShort(update.combined)
    }
}
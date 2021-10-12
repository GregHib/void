package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.FloorItemAddition
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.writeShortLittle

class FloorItemAdditionEncoder : ChunkEncoder<FloorItemAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: FloorItemAddition) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_ADD)
        writeShortLittle(update.floorItem.amount)
        writeShortLittle(update.floorItem.id)
        writeByte(update.floorItem.tile.offset())
    }
}
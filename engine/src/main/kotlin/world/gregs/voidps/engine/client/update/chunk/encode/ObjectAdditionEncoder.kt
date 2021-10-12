package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.ObjectAddition
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.writeByteAdd
import world.gregs.voidps.network.writeByteSubtract

class ObjectAdditionEncoder : ChunkEncoder<ObjectAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: ObjectAddition) = writer.run {
        writeByte(Protocol.Batch.OBJECT_ADD)
        writeByteSubtract((update.gameObject.type shl 2) or update.gameObject.rotation)
        writeShort(update.gameObject.id)
        writeByteAdd(update.gameObject.tile.offset())
    }
}
package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.ObjectRemoval
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.writeByteAdd

class ObjectRemoveEncoder : ChunkEncoder<ObjectRemoval> {
    override suspend fun encode(writer: ByteWriteChannel, update: ObjectRemoval) = writer.run {
        writeByte(Protocol.Batch.OBJECT_REMOVE)
        writeByteAdd((update.gameObject.type shl 2) or update.gameObject.rotation)
        writeByte(update.gameObject.tile.offset())
    }
}
package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.ObjectRemoval
import world.gregs.voidps.network.writeByteAdd

class ObjectRemoveEncoder : ChunkEncoder<ObjectRemoval> {
    override suspend fun encode(writer: ByteWriteChannel, update: ObjectRemoval) = writer.run {
        writeByte(Protocol.Batch.OBJECT_REMOVE)
        writeByteAdd((update.type shl 2) or update.rotation)
        writeByte(update.tileOffset)
    }
}
package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.ObjectAddition
import world.gregs.voidps.network.writeByteAdd
import world.gregs.voidps.network.writeByteSubtract

class ObjectAdditionEncoder : ChunkEncoder<ObjectAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: ObjectAddition) = writer.run {
        writeByte(Protocol.Batch.OBJECT_ADD)
        writeByteSubtract((update.type shl 2) or update.rotation)
        writeShort(update.id)
        writeByteAdd(update.tileOffset)
    }
}
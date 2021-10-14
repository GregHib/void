package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.ObjectAnimation
import world.gregs.voidps.network.writeByteInverse
import world.gregs.voidps.network.writeByteSubtract
import world.gregs.voidps.network.writeShortLittle

class ObjectAnimationEncoder : ChunkEncoder<ObjectAnimation> {
    override suspend fun encode(writer: ByteWriteChannel, update: ObjectAnimation) = writer.run {
        writeByte(Protocol.Batch.OBJECT_ANIMATION_SPECIFIC)
        writeShortLittle(update.id)
        writeByteSubtract(update.tileOffset)
        writeByteInverse((update.type shl 2) or update.rotation)
    }
}
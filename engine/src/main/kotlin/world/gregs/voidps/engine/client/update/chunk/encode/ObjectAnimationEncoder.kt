package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.ObjectAnimation
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.writeByteInverse
import world.gregs.voidps.network.writeByteSubtract
import world.gregs.voidps.network.writeShortLittle

class ObjectAnimationEncoder : ChunkEncoder<ObjectAnimation> {
    override suspend fun encode(writer: ByteWriteChannel, update: ObjectAnimation) = writer.run {
        writeByte(Protocol.Batch.OBJECT_ANIMATION_SPECIFIC)
        writeShortLittle(update.id)
        writeByteSubtract(update.gameObject.tile.offset())
        writeByteInverse((update.gameObject.type shl 2) or update.gameObject.rotation)
    }
}
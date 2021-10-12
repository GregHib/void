package world.gregs.voidps.engine.map.chunk

import io.ktor.utils.io.*

interface ChunkEncoder<C : ChunkUpdate> {

    suspend fun encode(writer: ByteWriteChannel, update: C)

}
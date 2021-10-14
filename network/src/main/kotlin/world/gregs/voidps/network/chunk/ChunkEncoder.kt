package world.gregs.voidps.network.chunk

import io.ktor.utils.io.*

interface ChunkEncoder<C : ChunkUpdate> {

    suspend fun encode(writer: ByteWriteChannel, update: C)

}
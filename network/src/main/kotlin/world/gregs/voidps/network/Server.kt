package world.gregs.voidps.network

import io.ktor.utils.io.*

interface Server {
    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String)
}

package world.gregs.voidps.network

import io.ktor.utils.io.*

data class ClientSession(
    val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    val cipherOut: IsaacCipher?
) {

    fun disconnect() {
        write.close()
    }

    fun flush() {

    }

}
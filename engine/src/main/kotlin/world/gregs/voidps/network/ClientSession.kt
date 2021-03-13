package world.gregs.voidps.network

import io.ktor.utils.io.*
import world.gregs.voidps.network.crypto.IsaacCipher

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
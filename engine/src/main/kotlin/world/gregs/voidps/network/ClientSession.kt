package world.gregs.voidps.network

import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import world.gregs.voidps.network.crypto.IsaacCipher

data class ClientSession(
    val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    val cipherOut: IsaacCipher?
) {

    val scope = CoroutineScope(Dispatchers.IO)

    fun disconnect() {
        write.close()
    }

}
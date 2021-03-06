package world.gregs.voidps.network

import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.network.crypto.IsaacCipher

data class ClientSession(
    val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    val cipherOut: IsaacCipher?
) {

    val scope = CoroutineScope(Contexts.Game)

    fun disconnect() {
        write.close()
    }

    fun flush() {

    }

}
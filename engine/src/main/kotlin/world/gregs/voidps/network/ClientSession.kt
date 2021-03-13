package world.gregs.voidps.network

import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import world.gregs.voidps.network.crypto.IsaacCipher

data class ClientSession(
    val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    val cipherOut: IsaacCipher?
) {

    val out = MutableSharedFlow<ByteWriteChannel>(replay = 10)

    fun disconnect() {
        GlobalScope.launch(Dispatchers.IO) {
            write.close()
        }
    }

    fun flush() {

    }

}
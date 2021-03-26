package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

data class Client(
    private val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    private val cipherOut: IsaacCipher?,
    val address: String
) {

    var exit: (() -> Unit)? = null
    private var connected = true
    private val logger = InlineLogger()
    private val handler = CoroutineExceptionHandler { _, throwable ->
        logger.warn { throwable.message }
        disconnect()
    }

    fun disconnect() {
        if (!connected) {
            return
        }
        write.flush()
        write.close()
        connected = false
    }

    fun exit() {
        exit?.invoke() ?: disconnect()
    }

    fun flush() {
        if (!connected) {
            return
        }
        write.flush()
    }

    fun send(opcode: Int, size: Int, type: Int = FIXED, block: suspend ByteWriteChannel.() -> Unit) {
        if (!connected) {
            return
        }

        runBlocking(Dispatchers.IO + handler) {
            write.header(opcode, type, size, cipherOut)
            block.invoke(write)
        }
    }

    private suspend fun ByteWriteChannel.header(opcode: Int, type: Int, size: Int, cipher: IsaacCipher?) {
        if (opcode < 0) {
            return
        }
        // Write opcode
        if (cipher != null) {
            if (opcode >= 128) {
                writeByte(((opcode shr 8) + 128) + cipher.nextInt())
                writeByte(opcode + cipher.nextInt())
            } else {
                writeByte(opcode + cipher.nextInt())
            }
        } else {
            writeSmart(opcode)
        }
        // Length
        when (type) {
            BYTE -> writeByte(size)
            SHORT -> writeShort(size)
        }
    }

    companion object {
        const val FIXED = 0
        const val BYTE = -1
        const val SHORT = -2

        fun smart(value: Int) = if (value >= 128) 2 else 1

        fun string(value: String?) = (value?.length ?: 0) + 1

        fun bits(bitCount: Int) = (bitCount + 7) / 8
    }
}
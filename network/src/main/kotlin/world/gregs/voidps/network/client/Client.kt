package world.gregs.voidps.network.client

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.network.login.protocol.writeByte
import world.gregs.voidps.network.login.protocol.writeShort
import world.gregs.voidps.network.login.protocol.writeSmart

open class Client(
    private val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    private val cipherOut: IsaacCipher?,
    val address: String,
) {

    private val logger = InlineLogger()
    private val handler = CoroutineExceptionHandler { _, throwable ->
        logger.warn { "Client error: ${throwable.message}" }
        runBlocking {
            disconnect()
        }
    }
    var disconnected: Boolean = false
    private var disconnect: (() -> Unit)? = null
    private var disconnecting: (suspend () -> Unit)? = null
    private var state: ClientState = ClientState.Connected

    fun onDisconnected(block: () -> Unit) {
        disconnect = block
    }

    fun onDisconnecting(block: suspend () -> Unit) {
        disconnecting = block
    }

    suspend fun disconnect(reason: Int) {
        if (disconnected) {
            return
        }
        write.writeByte(reason)
        disconnect()
    }

    suspend fun disconnect() {
        if (disconnected) {
            return
        }
        disconnected = true
        write.flushAndClose()
        state = ClientState.Disconnected
        disconnect?.invoke()
    }

    suspend fun exit() {
        if (state == ClientState.Connected) {
            state = ClientState.Disconnecting
            disconnecting?.invoke()
        }
    }

    open fun flush() {
        if (disconnected) {
            return
        }
        runBlocking {
            write.flush()
        }
    }

    open fun send(opcode: Int, block: suspend ByteWriteChannel.() -> Unit) = send(opcode, -1, FIXED, block)

    open fun send(opcode: Int, size: Int, type: Int, block: suspend ByteWriteChannel.() -> Unit) {
        if (disconnected) {
            return
        }
        runBlocking(handler) {
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

        fun name(displayName: String, responseName: String): Int = 1 + string(displayName) + if (displayName != responseName) string(responseName) else 0
    }
}

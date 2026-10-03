package world.gregs.voidps.network.client

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
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
    private val lock = Any()

    @Volatile
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
        disconnect?.invoke()
    }

    suspend fun exit() {
        if (state != ClientState.Connected) {
            return
        }
        state = ClientState.Disconnecting
        disconnecting?.invoke()
        state = ClientState.Disconnected
    }

    open fun flush() {
        if (disconnected) {
            return
        }
        write {
            flush()
        }
    }

    open fun send(opcode: Int, block: suspend ByteWriteChannel.() -> Unit) = send(opcode, -1, FIXED, block)

    open fun send(opcode: Int, size: Int, type: Int, block: suspend ByteWriteChannel.() -> Unit) {
        if (disconnected || state != ClientState.Connected) {
            return
        }
        write {
            header(opcode, type, size, cipherOut)
            block.invoke(this)
        }
    }

    /**
     * Writes to the channel, disconnecting if the connection has been closed or stopped reading
     * Locked so frames and [cipherOut] aren't interleaved by threads sending at the same time
     */
    private inline fun write(crossinline block: suspend ByteWriteChannel.() -> Unit) {
        synchronized(lock) {
            try {
                runBlocking {
                    withTimeout(WRITE_TIMEOUT_MS) {
                        block.invoke(write)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                logger.debug { "Client write timed out $address" }
                abort(e)
            } catch (e: IOException) {
                logger.debug { "Client write failed $address: ${e.message}" }
                runBlocking {
                    disconnect()
                }
            }
        }
    }

    /**
     * Close without flushing as the client isn't reading
     */
    private fun abort(cause: Throwable) {
        if (disconnected) {
            return
        }
        disconnected = true
        write.cancel(cause)
        disconnect?.invoke()
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
        private const val WRITE_TIMEOUT_MS = 2_000L

        fun smart(value: Int) = if (value >= 128) 2 else 1

        fun string(value: String?) = (value?.length ?: 0) + 1

        fun bits(bitCount: Int) = (bitCount + 7) / 8

        fun name(displayName: String, responseName: String): Int = 1 + string(displayName) + if (displayName != responseName) string(responseName) else 0
    }
}

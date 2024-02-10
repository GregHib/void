package world.gregs.voidps.network.client

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import world.gregs.voidps.network.writeSmart

open class Client(
    private val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    private val cipherOut: IsaacCipher?,
    val address: String
) {

    private val logger = InlineLogger()
    private val handler = context + CoroutineExceptionHandler { _, throwable ->
        logger.warn { "Client error: ${throwable.message}" }
        disconnect()
    }
    var disconnected: Boolean = false
    private val state = MutableStateFlow<ClientState>(ClientState.Connected)

    fun on(context: CoroutineDispatcher, state: ClientState, block: () -> Unit) = GlobalScope.launch(context) {
        this@Client.state
            .filter { it == state }
            .first()
        block.invoke()
    }

    suspend fun disconnect(reason: Int) {
        if (disconnected) {
            return
        }
        write.writeByte(reason)
        disconnect()
    }

    fun disconnect() {
        if (disconnected) {
            return
        }
        disconnected = true
        write.flush()
        write.close()
        state.tryEmit(ClientState.Disconnected)
    }

    fun exit() {
        if (state.value == ClientState.Connected) {
            state.tryEmit(ClientState.Disconnecting)
        }
    }

    open fun flush() {
        if (disconnected) {
            return
        }
        write.flush()
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
        @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
        val context = newSingleThreadContext("Networking")

        const val FIXED = 0
        const val BYTE = -1
        const val SHORT = -2

        fun smart(value: Int) = if (value >= 128) 2 else 1

        fun string(value: String?) = (value?.length ?: 0) + 1

        fun bits(bitCount: Int) = (bitCount + 7) / 8

        fun name(displayName: String, responseName: String): Int {
            return 1 + string(displayName) + if (displayName != responseName) string(responseName) else 0
        }
    }
}
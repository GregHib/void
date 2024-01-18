package world.gregs.voidps.network.file

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.Network.Companion.ACCEPT_SESSION
import world.gregs.voidps.network.readMedium
import world.gregs.voidps.network.readUByte
import world.gregs.voidps.network.readUMedium

class FileNetwork(
    private val revision: Int,
    private val prefetchKeys: IntArray,
    private val provider: FileProvider
) {

    val logger = InlineLogger()

    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        synchronise(read, write)
        if (acknowledge(read, write)) {
            logger.trace { "Client synchronisation complete: $hostname" }
            readRequests(read, write)
        }
    }

    /**
     * Confirm the client got our message and is ready to start sending file requests
     */
    private suspend fun acknowledge(read: ByteReadChannel, write: ByteWriteChannel): Boolean {
        val opcode = read.readByte().toInt()
        if (opcode != Network.ACKNOWLEDGE) {
            logger.trace { "Invalid ack opcode: $opcode" }
            write.writeByte(Network.REJECT_SESSION)
            write.close()
            return false
        }

        val id = read.readMedium()
        if (id != Network.ACKNOWLEDGE_ID) {
            logger.trace { "Invalid session id expected: ${Network.ACKNOWLEDGE_ID} actual: $id" }
            write.writeByte(Network.BAD_SESSION_ID)
            write.close()
            return false
        }
        return true
    }

    /**
     * If the client is up-to-date and in the correct state send it the [prefetchKeys] list so it knows what indices are available to request
     */
    private suspend fun synchronise(read: ByteReadChannel, write: ByteWriteChannel) {
        val revision = read.readInt()
        if (revision != this.revision) {
            logger.trace { "Invalid game revision: $revision" }
            write.writeByte(Network.GAME_UPDATED)
            write.close()
            return
        }

        write.writeByte(ACCEPT_SESSION)
        for (key in prefetchKeys) {
            write.writeInt(key)
        }
        write.flush()
    }

    private suspend fun readRequests(read: ByteReadChannel, write: ByteWriteChannel) = coroutineScope {
        try {
            while (isActive) {
                when (val opcode = read.readByte().toInt()) {
                    Network.STATUS_LOGGED_OUT, Network.STATUS_LOGGED_IN -> verify(read, write, Network.PREFETCH_REQUEST)
                    Network.PRIORITY_REQUEST, Network.PREFETCH_REQUEST -> {
                        val value = read.readUMedium()
                        provider.serve(write, value, opcode == Network.PREFETCH_REQUEST)
                    }
                    Network.ENCRYPTION_KEY_UPDATE -> read.readUByte()
                    else -> {
                        logger.warn { "Unknown file-server request $opcode." }
                        write.close()
                    }
                }
            }
        } finally {
            logger.trace { "Client disconnected: $read" }
        }
    }



    /**
     * Confirm a session value send by the client is as the server [expected]
     */
    suspend fun verify(read: ByteReadChannel, write: ByteWriteChannel, expected: Int): Boolean {
        val id = read.readMedium()
        if (id != expected) {
            logger.trace { "Invalid session id expected: $expected actual: $id" }
            write.writeByte(Network.BAD_SESSION_ID)
            write.close()
            return false
        }
        return true
    }
}
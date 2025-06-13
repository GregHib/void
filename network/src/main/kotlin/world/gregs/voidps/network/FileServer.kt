package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.network.file.FileProvider
import world.gregs.voidps.network.file.prefetchKeys
import world.gregs.voidps.network.login.protocol.readMedium
import world.gregs.voidps.network.login.protocol.readUByte
import world.gregs.voidps.network.login.protocol.readUMedium
import java.util.*

/**
 * Serves the client with the sector of cache files it has requested
 */
class FileServer(
    private val revision: Int,
    private val prefetchKeys: IntArray,
    private val provider: FileProvider,
) : Server {

    val logger = InlineLogger()

    override suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        synchronise(read, write)
        if (acknowledge(read, write)) {
            logger.trace { "Client synchronisation complete: $hostname" }
            readRequests(read, write, hostname)
        }
    }

    /**
     * Confirm the client got our message and is ready to start sending file requests
     */
    private suspend fun acknowledge(read: ByteReadChannel, write: ByteWriteChannel): Boolean {
        val opcode = read.readByte().toInt()
        if (opcode != Request.ACKNOWLEDGE) {
            logger.trace { "Invalid ack opcode: $opcode" }
            write.writeByte(Response.LOGIN_SERVER_REJECTED_SESSION)
            write.close()
            return false
        }

        val id = read.readMedium()
        if (id != ACKNOWLEDGE_ID) {
            logger.trace { "Invalid session id expected: $ACKNOWLEDGE_ID actual: $id" }
            write.writeByte(Response.BAD_SESSION_ID)
            write.close()
            return false
        }
        return true
    }

    /**
     * If the client is up-to-date and in the correct state send it the [prefetchKeys] list, so it knows what indices are available to request
     */
    private suspend fun synchronise(read: ByteReadChannel, write: ByteWriteChannel) {
        val revision = read.readInt()
        if (revision != this.revision) {
            logger.trace { "Invalid game revision: $revision" }
            write.writeByte(Response.GAME_UPDATE)
            write.close()
            return
        }

        write.writeByte(Response.DATA_CHANGE)
        for (key in prefetchKeys) {
            write.writeInt(key)
        }
        write.flush()
    }

    private suspend fun readRequests(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) = coroutineScope {
        try {
            while (isActive) {
                when (val opcode = read.readByte().toInt()) {
                    Request.STATUS_LOGGED_OUT, Request.STATUS_LOGGED_IN -> verify(read, write, Request.PREFETCH_REQUEST)
                    Request.PRIORITY_REQUEST, Request.PREFETCH_REQUEST -> {
                        val value = read.readUMedium()
                        provider.serve(write, value, opcode == Request.PREFETCH_REQUEST)
                    }
                    Request.ENCRYPTION_KEY_UPDATE -> read.readUByte()
                    else -> {
                        logger.warn { "Unknown file-server request $opcode." }
                        read.cancel()
                        write.close()
                    }
                }
            }
        } finally {
            logger.trace { "Client disconnected: $hostname." }
        }
    }

    /**
     * Confirm a session value send by the client is as the server [expected]
     */
    suspend fun verify(read: ByteReadChannel, write: ByteWriteChannel, expected: Int): Boolean {
        val id = read.readMedium()
        if (id != expected) {
            logger.trace { "Invalid session id expected: $expected actual: $id" }
            write.writeByte(Response.BAD_SESSION_ID)
            write.close()
            return false
        }
        return true
    }

    companion object {
        private const val ACKNOWLEDGE_ID = 3

        fun load(cache: Cache, properties: Properties): Server {
            val fileServer = properties.getProperty("storage.cache.server")
            if (fileServer == "external") {
                return offlineFileServer()
            }
            val fileProvider: FileProvider = FileProvider.load(cache, properties)
            val revision = properties.getProperty("server.revision").toInt()
            val prefetchKeys = prefetchKeys(cache, properties)
            return FileServer(revision, prefetchKeys, fileProvider)
        }

        private fun offlineFileServer() = object : Server {
            override suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
                write.writeByte(Response.LOGIN_SERVER_OFFLINE)
                write.close()
            }
        }
    }
}

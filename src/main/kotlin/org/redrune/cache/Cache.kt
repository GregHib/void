package org.redrune.cache

import com.alex.store.Store
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.redrune.GameConstants
import org.redrune.network.packet.struct.OutgoingPacket
import kotlin.experimental.and

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object Cache : Store(GameConstants.CACHE_DIRECTORY) {

    /**
     * The update keys byte array
     */
    private var ukeysFile: ByteArray? = null

    /**
     * Constructs the [ukeysFile] lazily and gets the data from it
     * @return ByteBuf
     */
    @JvmStatic
    fun getUkeysFile(): ByteBuf {
        if (ukeysFile == null) {
            ukeysFile = generateUkeysFile()
        }
        return getContainerPacketData(255, 255, ukeysFile!!)
    }

    /**
     * Generates index 255, archive 255 data
     * @return ByteArray
     */
    private fun generateUkeysFile(): ByteArray {
        return generateIndex255Archive255Current(null, null);
    }

    /**
     * Creates a buffer with data in the specified cache location
     */
    @Suppress("SameParameterValue")
    private fun getContainerPacketData(indexFileId: Int, containerId: Int, archive: ByteArray): ByteBuf {
        val buffer = Unpooled.buffer(archive.size + 4)
        buffer.writeByte(indexFileId)
        buffer.writeShort(containerId)
        buffer.writeByte(0)
        buffer.writeInt(archive.size)
        for (index in archive.indices) {
            if (buffer.writerIndex() % 512 == 0) {
                buffer.writeByte(255)
            }
            buffer.writeByte(archive[index].toInt())
        }
        return buffer
    }

    /**
     * Gets the {@code Packet} instance of the cache archive located in the parameterized places
     */
    fun getCacheArchive(indexId: Int, archiveId: Int, priority: Boolean): ByteBuf? {
        return if (indexId == 255 && archiveId == 255) {
            getUkeysFile()
        } else {
            val packet = getArchivePacketData(indexId, archiveId, priority)
                ?: throw IllegalStateException("Unable to send cache archive [$indexId, $archiveId, $priority]")
            return packet.buffer
        }
    }

    /**
     * Gets the data in the archive and index
     */
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    fun getArchivePacketData(indexId: Int, archiveId: Int, priority: Boolean): OutgoingPacket? {
        val archive: ByteArray =
            (if (indexId == 255) index255 else indexes[indexId].mainFile).getArchiveData(archiveId) ?: return null
        val compression: Int = (archive[0] and 0xff.toByte()).toInt()
        val length: Int =
            ((archive[1] and 0xff.toByte()).toInt() shl 24) + ((archive[2] and 0xff.toByte()).toInt() shl 16) + ((archive[3] and 0xff.toByte()).toInt() shl 8) + (archive[4] and 0xff.toByte())
        var settings = compression
        if (!priority) {
            settings = settings or 0x80
        }
        val packet = OutgoingPacket()
        packet.writeByte(indexId)
        packet.writeShort(archiveId)
        packet.writeByte(settings)
        packet.writeInt(length)
        val realLength = if (compression != 0) length + 4 else length
        for (index in 5 until realLength + 5) {
            if (packet.position() % 512 === 0) {
                packet.writeByte(255)
            }
            packet.writeByte(archive[index].toInt())
        }
        return packet
    }
}
package org.redrune.cache

import com.alex.store.Store
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.redrune.GameConstants
import org.redrune.cache.secure.RSA
import org.redrune.cache.secure.Whirlpool
import org.redrune.network.packet.struct.OutgoingPacket
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.math.BigInteger
import java.nio.Buffer
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object Cache : Store(GameConstants.CACHE_DIRECTORY) {

    /**
     * The modulus value for rsa
     */
    private val rsaMod = BigInteger(GameConstants.RSA_MODULUS, 16)

    /**
     * The private rsa key
     */
    private val rsaPriv = BigInteger(GameConstants.RSA_PRIVATE, 16)

    /**
     * The version table data
     */
    private val versionTable = createVersionTable(true, rsaMod, rsaPriv)

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
    fun getArchive(indexId: Int, archiveId: Int, priority: Boolean): ByteBuf? {
        println("indexId = [${indexId}], archiveId = [${archiveId}], priority = [${priority}]")
        return if (indexId == 255 && archiveId == 255) {
            println("$versionTable")
            Unpooled.copiedBuffer(versionTable)
        } else if (indexId == 255) {
            println("${index255.getArchiveData(archiveId)}")
            Unpooled.copiedBuffer(index255.getArchiveData(archiveId))
        } else {
            println("${indexes[indexId].mainFile.getArchiveData(archiveId)}")
            Unpooled.copiedBuffer(indexes[indexId].mainFile.getArchiveData(archiveId))
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

    /**
     * Generating the version table data
     * @return ByteArray
     */
    private fun createVersionTable(whirlpool: Boolean, modulus: BigInteger?, private: BigInteger?): ByteArray {
        val bout = ByteArrayOutputStream()
        DataOutputStream(bout).use { buffer ->
            run {
                if (whirlpool) {
                    buffer.writeByte(indexes.size)
                }

                for (i in 0 until indexes.size) {
                    buffer.writeInt(indexes[i].crc)
                    buffer.writeInt(indexes[i].table?.revision ?: 0)
                    if (whirlpool) {
                        buffer.write(indexes[i].whirlpool ?: ByteArray(64))
                        //keys?
                    }
                }
            }

            if (whirlpool) {
                val bytes = bout.toByteArray()
                var temp = ByteBuffer.allocate(65)
                temp.put(1)
                temp.put(Whirlpool.whirlpool(bytes, 0, bytes.size))
                (temp as Buffer).flip()

                if (modulus != null && private != null) {
                    temp = RSA.crypt(temp, modulus, private)
                }

                buffer.write(temp.array())
            }

            val data = bout.toByteArray()
            val out = ByteBuffer.allocate(5 + data.size)
            out.put(0)
            out.putInt(data.size)
            out.put(data)
            return out.array()
        }
    }
}
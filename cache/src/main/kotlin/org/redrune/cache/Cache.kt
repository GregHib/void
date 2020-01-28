package org.redrune.cache

import mu.KotlinLogging
import org.displee.CacheLibrary
import org.redrune.cache.secure.RSA
import org.redrune.cache.secure.Whirlpool
import org.redrune.tools.constants.GameConstants
import org.redrune.tools.constants.NetworkConstants.Companion.FILE_SERVER_RSA_MODULUS
import org.redrune.tools.constants.NetworkConstants.Companion.FILE_SERVER_RSA_PRIVATE
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.math.BigInteger
import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object Cache : CacheLibrary(GameConstants.CACHE_DIRECTORY) {

    private val logger = KotlinLogging.logger {}

    private val versionTable: ByteArray = createVersionTable(FILE_SERVER_RSA_MODULUS, FILE_SERVER_RSA_PRIVATE)

    fun getFile(indexId: Int, archiveId: Int): ByteArray? {
        if (indexId == 255 && archiveId == 255) {
            return versionTable
        }
        val index = if (indexId == 255) checksumTable else indices[indexId]
        if (index == null) {
            logger.warn { "Unable to find valid index for file request [indexId=$indexId, archiveId=$archiveId]}" }
            return null
        }
        val archiveSector = index.readArchiveSector(archiveId)
        if (archiveSector == null) {
            logger.warn { "Unable to read archive sector $archiveId in index $indexId" }
            return null
        }
        return archiveSector.data
    }

        private fun createVersionTable(modulus: BigInteger, private: BigInteger): ByteArray {
            val bout = ByteArrayOutputStream()
            DataOutputStream(bout).use { buffer ->
                Cache.run {
                    buffer.writeByte(indices.size)

                    for (i in indices.indices) {
                        buffer.writeInt(indices[i].crc)
                        buffer.writeInt(indices[i].revision)
                        buffer.write(indices[i].whirlpool ?: ByteArray(64))
                    }
                }

                val bytes = bout.toByteArray()
                var temp = ByteBuffer.allocate(65)
                temp.put(1)
                temp.put(Whirlpool.whirlpool(bytes, 0, bytes.size))
                (temp as Buffer).flip()

                temp = RSA.crypt(temp, modulus, private)

                buffer.write(temp.array())

                val data = bout.toByteArray()
                val out = ByteBuffer.allocate(5 + data.size)
                out.put(0)
                out.putInt(data.size)
                out.put(data)
                return out.array()
            }
        }

        fun valid(index: Int, archive: Int): Boolean {
            if (archive < 0) {
                return false
            }
            if (index != 255) {
                if (Cache.indices.size <= index || Cache.indices[index].getArchive(archive) == null) {
                    return false
                }
            } else if (archive != 255) {
                if (Cache.indices.size <= archive) {
                    return false
                }
            }
            return true
        }

    }
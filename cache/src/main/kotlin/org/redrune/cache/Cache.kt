package org.redrune.cache

import com.alex.store.Store
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
object Cache : Store(GameConstants.CACHE_DIRECTORY) {

    val versionTable: ByteArray = createVersionTable(true, FILE_SERVER_RSA_MODULUS, FILE_SERVER_RSA_PRIVATE)

    fun getFile(indexId: Int, archiveId: Int): ByteArray {
        if (indexId == 255 && archiveId == 255) {
            return versionTable
        }
        if (indexId == 255) {
            return index255.getArchiveData(archiveId)
        }
        return indexes[indexId].mainFile.getArchiveData(archiveId)
    }

    private fun createVersionTable(whirlpool: Boolean = true, modulus: BigInteger?, private: BigInteger?): ByteArray {
        val bout = ByteArrayOutputStream()
        DataOutputStream(bout).use { buffer ->
            Cache.run {
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
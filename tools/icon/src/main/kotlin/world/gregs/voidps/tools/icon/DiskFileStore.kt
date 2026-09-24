package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.math.min

/**
 * A [ResourceProvider] that reads a single archive/index straight out of a
 * classic on-disk Jagex 555 cache (main_file_cache.dat2 + main_file_cache.idxN),
 * with no OnDemand/network/RSA verification involved. Lets the standalone
 * tooling (see [CacheItemSpriteDumper]) build [Js5] archives
 * directly from a cache directory instead of downloading via the client's
 * normal OnDemand bootstrap ([Class340]/[Class369_Sub2]).
 */
internal class DiskFileStore(cacheDir: String?, private val idx: Int, private val cache: Cache?) : ResourceProvider() {
    private val data: RandomAccessFile
    private val index: RandomAccessFile
    private val indexMeta: RandomAccessFile
    private var referenceTable: Js5Index? = null

    init {
        data = RandomAccessFile(File(cacheDir, "main_file_cache.dat2"), "r")
        index = RandomAccessFile(File(cacheDir, "main_file_cache.idx" + idx), "r")
        indexMeta = RandomAccessFile(File(cacheDir, "main_file_cache.idx" + INDEX_META), "r")
    }

    override fun method2335(i: Int, i_0_: Int): Int {
        return 100
    }

    override fun method2338(i: Byte, i_6_: Int) {
        /* no preload queue needed for direct disk reads */
    }

    override fun fetchGroup(file: Int, i_7_: Byte): ByteArray? {
//        System.out.println("Load " + file + " " + idx);
        try {
            val data = read(index, file, idx)
            //            System.out.println("Expected: " + Arrays.toString(data));
            return data
        } catch (ioexception: IOException) {
            throw RuntimeException("Failed reading archive " + idx + " file " + file, ioexception)
        }
        //        byte[] data1 = cache.data(idx, file, 0, null);
//        System.out.println("Actual: " + Arrays.toString(data1));
//        return data1;//read(index, file, archive);
    }

    override fun index(i: Byte): Js5Index? {
        if (referenceTable != null) return referenceTable
        try {
            val raw = read(indexMeta, idx, INDEX_META)
            if (raw == null) return null
            val crc = Class59_Sub1.method554(5126, raw.size, raw)
            referenceTable = Js5Index(raw, crc, null)
            return referenceTable
        } catch (ioexception: IOException) {
            throw RuntimeException("Failed reading reference table for archive " + idx, ioexception)
        }
    }

    @Throws(IOException::class)
    private fun read(idxFile: RandomAccessFile, fileId: Int, expectedIndexId: Int): ByteArray? {
        val entryOffset = fileId.toLong() * 6
        if (entryOffset + 6 > idxFile.length()) return null
        val entry = ByteArray(6)
        idxFile.seek(entryOffset)
        idxFile.readFully(entry)
        val size = ((entry[0].toInt() and 0xff) shl 16) or ((entry[1].toInt() and 0xff) shl 8) or (entry[2].toInt() and 0xff)
        var sector = ((entry[3].toInt() and 0xff) shl 16) or ((entry[4].toInt() and 0xff) shl 8) or (entry[5].toInt() and 0xff)
        if (size == 0 || sector == 0) return null

        val result = ByteArray(size)
        val buf = ByteArray(SECTOR_SIZE)
        var remaining = size
        var chunk = 0
        val extended = fileId > 0xffff
        while (remaining > 0) {
            if (sector == 0) throw IOException("Unexpected end of sector chain (archive " + expectedIndexId + ", file " + fileId + ")")
            data.seek(sector.toLong() * SECTOR_SIZE)
            data.readFully(buf)

            val currentFile: Int
            val currentChunk: Int
            val nextSector: Int
            val currentIndex: Int
            val headerSize: Int
            val dataSize: Int
            if (extended) {
                currentFile = ((buf[0].toInt() and 0xff) shl 24) or ((buf[1].toInt() and 0xff) shl 16) or ((buf[2].toInt() and 0xff) shl 8) or (buf[3].toInt() and 0xff)
                currentChunk = ((buf[4].toInt() and 0xff) shl 8) or (buf[5].toInt() and 0xff)
                nextSector = ((buf[6].toInt() and 0xff) shl 16) or ((buf[7].toInt() and 0xff) shl 8) or (buf[8].toInt() and 0xff)
                currentIndex = buf[9].toInt() and 0xff
                headerSize = EXTENDED_HEADER_SIZE
                dataSize = EXTENDED_DATA_SIZE
            } else {
                currentFile = ((buf[0].toInt() and 0xff) shl 8) or (buf[1].toInt() and 0xff)
                currentChunk = ((buf[2].toInt() and 0xff) shl 8) or (buf[3].toInt() and 0xff)
                nextSector = ((buf[4].toInt() and 0xff) shl 16) or ((buf[5].toInt() and 0xff) shl 8) or (buf[6].toInt() and 0xff)
                currentIndex = buf[7].toInt() and 0xff
                headerSize = HEADER_SIZE
                dataSize = DATA_SIZE
            }
            if (currentFile != fileId || currentChunk != chunk || currentIndex != expectedIndexId) {
                throw IOException("Sector chain corrupt reading archive " + expectedIndexId + " file " + fileId + " at sector " + sector)
            }
            val len = min(remaining, dataSize)
            System.arraycopy(buf, headerSize, result, size - remaining, len)
            remaining -= len
            sector = nextSector
            chunk++
        }
        return result
    }

    companion object {
        private const val SECTOR_SIZE = 520
        private const val HEADER_SIZE = 8
        private val DATA_SIZE: Int = SECTOR_SIZE - HEADER_SIZE
        private const val EXTENDED_HEADER_SIZE = 10
        private val EXTENDED_DATA_SIZE: Int = SECTOR_SIZE - EXTENDED_HEADER_SIZE
        private const val INDEX_META = 255
    }
}

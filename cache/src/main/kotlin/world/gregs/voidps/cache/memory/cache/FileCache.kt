package world.gregs.voidps.cache.memory.cache

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.cache.memory.load.ThreadContext
import java.io.RandomAccessFile

class FileCache(
    private val main: RandomAccessFile,
    private val indexes: Array<RandomAccessFile?>,
    indexCount: Int,
    val xteas: Map<Int, IntArray>?
) : ReadOnlyCache(IntArray(indexCount) { it }, arrayOfNulls(indexCount), arrayOfNulls(indexCount), arrayOfNulls(indexCount), Int2IntOpenHashMap(16384)) {

    private val dataCache = object : LinkedHashMap<Int, Array<ByteArray?>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Array<ByteArray?>>?): Boolean {
            return size > 12
        }
    }
    private val length = main.length()
    private val context = ThreadContext()

    private fun hash(index: Int, archive: Int) = index + (archive shl 6)

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        val matchingIndex = files.getOrNull(index)?.getOrNull(archive)?.indexOf(file) ?: -1
        if (matchingIndex == -1) {
            return null
        }
        val files = dataCache.getOrPut(hash(index, archive)) {
            val indexRaf = indexes[index] ?: return null

            val fileCounts = fileCounts.getOrNull(index) ?: return null
            val fileIds = files.getOrNull(index) ?: return null
            readFileData(fileCounts, fileIds, index, archive, main, length, indexRaf, xteas, context) ?: return null
        }
        return files[matchingIndex]
    }
}
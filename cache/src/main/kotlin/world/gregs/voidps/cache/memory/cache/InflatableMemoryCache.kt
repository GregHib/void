package world.gregs.voidps.cache.memory.cache

import java.util.zip.Inflater

class InflatableMemoryCache(
    val data: Array<Array<Array<ByteArray?>?>?>,
    indices: IntArray,
    archives: Array<IntArray?>,
    fileCounts: Array<IntArray?>,
    files: Array<Array<IntArray?>?>,
    hashes: Map<Int, Int>?
) : ReadOnlyCache(indices, archives, fileCounts, files, hashes) {
    private val inflater = Inflater(true)
    private val temp = ByteArray(5_000_000)

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        val data = data[index]?.get(archive)?.get(file) ?: return null
        inflater.setInput(data)
        inflater.finished()
        val size = inflater.inflate(temp)
        inflater.reset()
        return temp.copyOf(size)
    }

}
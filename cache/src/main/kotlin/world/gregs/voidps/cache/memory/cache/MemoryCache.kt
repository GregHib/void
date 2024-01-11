package world.gregs.voidps.cache.memory.cache

class MemoryCache(
    val data: Array<Array<Array<ByteArray?>?>?>,
    indices: IntArray,
    archives: Array<IntArray?>,
    fileCounts: Array<IntArray?>,
    files: Array<Array<IntArray?>?>,
    hashes: Map<Int, Int>?
) : ReadOnlyCache(indices, archives, fileCounts, files, hashes) {

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        return data.getOrNull(index)?.getOrNull(archive)?.getOrNull(file)
    }

}
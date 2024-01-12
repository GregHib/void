package world.gregs.voidps.cache.memory.cache

class MemoryCache(indexCount: Int) : ReadOnlyCache(indexCount) {

    val data: Array<Array<Array<ByteArray?>?>?> = arrayOfNulls(indexCount)

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        return data.getOrNull(index)?.getOrNull(archive)?.getOrNull(file)
    }

}
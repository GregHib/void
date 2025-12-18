package world.gregs.voidps.cache.type

import world.gregs.voidps.cache.Cache

interface LoaderConfig<T : Type> {
    val paths: List<String>
    val lastModified: Long
    val bufferSize: Int
    val index: Int
    val name: String
    fun size(cache: Cache): Int
    fun file(id: Int) : Int
    fun archive(id: Int) : Int
    fun create(size: Int, block: (Int) -> T? = { null }): Array<T?>
    fun decoder(): TypeDecoder<T>
}
